package common.nw.installer;

import argo.format.PrettyJsonFormatter;
import argo.jdom.*;
import argo.saj.InvalidSyntaxException;
import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.gson.Gson;
import common.nw.core.modpack.Library;
import common.nw.core.modpack.LocalModpack;
import common.nw.core.modpack.ModpackValues;
import common.nw.core.modpack.RepoModpack;
import common.nw.core.utils.DownloadHelper;
import common.nw.core.utils.FileUtils;
import common.nw.core.utils.Utils;
import common.nw.core.utils.log.NwLogHelper;
import common.nw.core.utils.log.NwLogger;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.List;

public class Installer {

	private static final String JSON_MC_ARGUMENTS = "--username ${auth_player_name} --version ${version_name} --gameDir ${game_directory} --assetsDir ${assets_root} --assetIndex ${assets_index_name} --uuid ${auth_uuid} --accessToken ${auth_access_token} --userProperties ${user_properties} --userType ${user_type} --tweakClass common.nw.updater.launch.Launch --tweakClass cpw.mods.fml.common.launcher.FMLTweaker --modpackrepo %s --modpackversion %s";
	private static final String JSON_TYPE = "release";
	private static final String JSON_TIME = "2015-12-10T00:05:37-0500";

	/**
	 * version Name (mc-launcher)
	 */
	private String name;

	/**
	 * .minecraft Path
	 */
	private String dir;

	/**
	 * should a profile be created?
	 * (mc-launcher)
	 */

	private boolean createProfile;

	/**
	 * should the libs be downloaded?
	 */
	private boolean downloadLib;


	/**
	 * .minecraft folder
	 */
	private File minecraftDirectory;

	/**
	 * our VersionDirectory (mc-launcher)
	 */
	private File ourDir;

	private RepoModpack repo;

	/**
	 * json-version information (mc-launcher)
	 */
	private JsonRootNode data;

	/**
	 * download url of this modpack
	 */
	private String modpackUrl;


	public Installer(RepoModpack repo, String name, String dir, String url,
	                 boolean createProfile, boolean downloadLib) {
		this.repo = repo;
		this.name = name;
		this.dir = dir;
		this.modpackUrl = url;
		this.createProfile = createProfile;
		minecraftDirectory = new File(dir);
		this.downloadLib = downloadLib;
	}

	/**
	 * downloads modpack.json file
	 *
	 * @return success
	 */
	public static RepoModpack downloadModpack(String url) {
		try {
			String json = null;
			if (!url.startsWith("http:") && !url.startsWith("www.") && !url.startsWith("https:") || !url.contains("/")) {
				//try and read local file
				NwLogger.INSTALLER_LOGGER.info("Modpack URL does not seem to be an internet url! Trying to get local File");
				json = DownloadHelper.getStringFromFile(url, null);
			}
			if (json == null || json.isEmpty()) {
				json = DownloadHelper.getString(url, null);
			}
			if (json == null || json.isEmpty()) {
				return null;
			}
			Gson gson = new Gson();
			return gson.fromJson(json, RepoModpack.class);
		} catch (Exception e) {
			NwLogger.INSTALLER_LOGGER.error("Error downloading Modpack.json", e);
		}
		return null;
	}

	/**
	 * are all entries valid?
	 */
	public boolean validateEntries() {
		boolean notNull = name != null && !name.isEmpty() && dir != null
				&& !dir.isEmpty();
		if (notNull) {
			minecraftDirectory = new File(dir);
			return minecraftDirectory.exists() && minecraftDirectory.isDirectory();
		}
		return false;
	}

	/**
	 * create needed dirs
	 *
	 * @return success
	 */
	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	public boolean createDirs() {
		File versions = new File(minecraftDirectory, "versions");
		if (!versions.exists()) {
			if (!versions.mkdir()) {
				return false;
			}
		}
		ourDir = new File(versions, name);
		if (!ourDir.exists()) {
			if (!ourDir.mkdir()) {
				NwLogHelper.severe("Error creating version directory!");
				return false;
			}
		} else {
			//compensate wrong upper/lower case
			if (!Utils.deleteFileOrDir(ourDir)) {
				NwLogHelper.error("Error deleting old version dir!");
				return false;
			}
			if (!ourDir.mkdir()) {
				NwLogHelper.error("Error recreating version dir!");
				return false;
			}
		}
		return true;
	}

	/**
	 * downloads libraries (only nw-updater atm)
	 *
	 * @return success
	 */
	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	public boolean downloadLibraries() {
		if (!downloadLib) {
			return true;
		}
		try {
			List<JsonNode> libCopy = data.getArrayNode("libraries");
			for (JsonNode node : libCopy) {
				final String libName = node.getStringValue("name");
				if (libName.contains("common.nuklearwurst:updater")) {
					String url = node.getStringValue("url");
					if (url.endsWith("/")) {
						url = url.substring(0, url.length() - 1);
					}
					File libraryDirectory = new File(minecraftDirectory, "libraries");
					if (!FileUtils.createDirectoryIfNecessary(libraryDirectory)) {
						NwLogger.INSTALLER_LOGGER.error("Error creating library directory!");
						return false;
					}
					final String version = libName.substring(libName.lastIndexOf(":") + 1);
					File updaterDirectory = new File(libraryDirectory, "common" + File.separator + "nuklearwurst" + File.separator + "updater" + File.separator + version);
					if (!FileUtils.createDirectoriesIfNecessary(updaterDirectory)) {
						NwLogger.INSTALLER_LOGGER.error("Error creating library directory!");
						return false;
					}
					File updaterJarFile = new File(updaterDirectory, "updater-" + version + ".jar");
					String realUrl = url + "/common/nuklearwurst/updater/" + version + "/updater-" + version + ".jar";
					if (!DownloadHelper.downloadFile(realUrl, updaterJarFile)) {
						NwLogger.INSTALLER_LOGGER.error("Error downloading updater jar-file!");
						return false;
					}
				}
			}
		} catch (Exception e) {
			NwLogger.INSTALLER_LOGGER.error("Unknown error when downloading libraries!", e);
			return false;
		}
		return true;
	}


	/**
	 * downloads and parses json version file
	 * <p/>
	 * note: does not write json file to disk
	 *
	 * @see {@link #writeJson()}
	 */
	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	public boolean createJson() {
		JdomParser parser = new JdomParser();
		JsonRootNode versionJson;
		HashMap<JsonStringNode, JsonNode> versionDataCopy;

		if (ModpackValues.jsonGenerate.equals(repo.minecraft.jsonUpdateType)) {
			//Generate new version-json file
			versionDataCopy = Maps.newHashMap();
			versionDataCopy.put(JsonNodeFactories.string("time"), JsonNodeFactories.string(JSON_TIME));
			versionDataCopy.put(JsonNodeFactories.string("type"), JsonNodeFactories.string(JSON_TYPE));
			versionDataCopy.put(JsonNodeFactories.string("minecraftArguments"), JsonNodeFactories.string(String.format(JSON_MC_ARGUMENTS, modpackUrl, repo.minecraft.version)));

		} else if (ModpackValues.jsonDirectDownload.equals(repo.minecraft.jsonUpdateType)) {
			//download version-json file
			try {
				String jsonString = DownloadHelper.getString(repo.minecraft.jsonName, null);
				versionJson = parser.parse(jsonString);
				versionDataCopy = Maps.newHashMap(versionJson.getFields());
			} catch (InvalidSyntaxException e) {
				NwLogger.INSTALLER_LOGGER.error("Error parsing version file!", e);
				return false;
			} catch (IOException e) {
				NwLogger.INSTALLER_LOGGER.error("Error downloading version.json", e);
				return false;
			} catch (Exception e) {
				NwLogger.INSTALLER_LOGGER.error("Unknown error downloading version.json", e);
				return false;
			}
		} else {
			NwLogger.INSTALLER_LOGGER.error("Unsupported version format: " + repo.minecraft.jsonUpdateType);
			return false;
		}

		//Add libraries
		final JsonStringNode libs = JsonNodeFactories.string("libraries");
		final JsonNode libraryNode = versionDataCopy.get(libs);
		final List<JsonNode> libraryList;
		if (libraryNode == null) {
			libraryList = new ArrayList<>();
		} else {
			libraryList = libraryNode.getElements();
		}

		try {
			libraryList.addAll(Library.parseJsonListFromStrings(repo.minecraft.libraries));
		} catch (InvalidSyntaxException e) {
			NwLogger.INSTALLER_LOGGER.error("Error reading modpack libraries!", e);
			return false;
		}
		versionDataCopy.put(libs, JsonNodeFactories.array(libraryList));


		//Add inheritance if needed
		if (repo.minecraft.jarUpdateType.equals(ModpackValues.jarForgeInherit)) {
			String forgeVersion = null;
			try {
				//noinspection StatementWithEmptyBody
				if (repo.minecraft.versionName.contains("/")) {
					//this seems to be direct link, we don't know which version
				} else if (repo.minecraft.versionName.contains("-")) {
					//parse as full version name
					forgeVersion = repo.minecraft.versionName;
				} else {
					//parse as build number
					String s = DownloadHelper.getString(ModpackValues.URL_FORGE_VERSION_JSON, null);
					JdomParser forgeParser = new JdomParser();
					JsonRootNode forgeVersionData = forgeParser.parse(s);
					JsonNode build = forgeVersionData.getNode("number", repo.minecraft.versionName);
					String branch = build.getStringValue("branch");
					if (branch == null) {
						branch = "";
					} else {
						branch = "-" + branch;
					}
					String mcversion = build.getStringValue("mcversion");
					String forgeversion = build.getStringValue("version");
					forgeVersion = mcversion + "-Forge" + forgeversion + branch;
				}
			} catch (MalformedURLException e) {
				NwLogger.INSTALLER_LOGGER.error("Error parsing Minecraft Forge Installer version...", e);
			} catch (IOException e) {
				NwLogger.INSTALLER_LOGGER.error("Error reading Minecraft Forge Version Data", e);
			} catch (InvalidSyntaxException e) {
				NwLogger.INSTALLER_LOGGER.error("Error parsing Minecraft Forge Version Data", e);
			} catch (NumberFormatException e) {
				NwLogger.INSTALLER_LOGGER.error("Error parsing Minecraft Forge Build Number", e);
			} catch (IllegalArgumentException e) {
				NwLogger.INSTALLER_LOGGER.error("Error parsing Minecraft Forge Version Data", e);
			} catch (Exception e) {
				NwLogger.INSTALLER_LOGGER.error("Unknown Error occurred!", e);
			}
			if (forgeVersion != null) {
				versionDataCopy.put(JsonNodeFactories.string("inheritsFrom"), JsonNodeFactories.string(forgeVersion));
			}
		}
		//Add Version Id
		versionDataCopy.put(JsonNodeFactories.string("id"), JsonNodeFactories.string(name));
		versionJson = JsonNodeFactories.object(versionDataCopy);

		//save json file data for later use
		data = versionJson;
		return true;
	}

	/**
	 * writes the saved json file to disk
	 *
	 * @return success
	 */
	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	public boolean writeJson() {
		if (data == null) {
			NwLogger.INSTALLER_LOGGER.error("Error writing json-file: No data available!");
			return false;
		}
		try {
			File file = new File(ourDir, name + ".json");
			if (file.exists()) {
				if (!file.delete()) {
					return false;
				}
			}
			BufferedWriter newWriter = Files.newWriter(file, Charsets.UTF_8);
			PrettyJsonFormatter.fieldOrderPreservingPrettyJsonFormatter().format(data, newWriter);
			newWriter.close();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * download version jar
	 *
	 * @return true if successful
	 */
	@SuppressWarnings({"unchecked", "BooleanMethodIsAlwaysInverted"})
	public boolean createJar(boolean allowGui, Component parentWindow) {
		//delete old file
		//FIXME: this might create errors with forge installs and maybe should be removed
		File file = new File(ourDir, name + ".jar");
		if (file.exists()) {
			if (!file.delete()) {
				return false;
			}
		}
		if (repo.minecraft.versionName != null && !repo.minecraft.versionName.isEmpty()) {
			if (repo.minecraft.jarUpdateType != null) {
				if (repo.minecraft.jarUpdateType.equals(ModpackValues.jarForgeInherit)) {
					NwLogger.INSTALLER_LOGGER.info("Starting Minecraft Forge Installation.");
					try {
						URL url;
						if (repo.minecraft.versionName.contains("/")) {
							//parse as direct Link
							url = new URL(repo.minecraft.versionName);
						} else if (repo.minecraft.versionName.contains("-")) {
							//parse as full version name
							url = new URL(ModpackValues.URL_FORGE_INSTALLER + repo.minecraft.versionName + "/forge-" + repo.minecraft.versionName + "-installer.jar");
						} else {
							//parse as build number
							String s = DownloadHelper.getString(ModpackValues.URL_FORGE_VERSION_JSON, null);
							JdomParser parser = new JdomParser();
							JsonRootNode versionData = parser.parse(s);
							JsonNode build = versionData.getNode("number", repo.minecraft.versionName);
							String branch = build.getStringValue("branch");
							if (branch == null) {
								branch = "";
							} else {
								branch = "-" + branch;
							}
							String mcversion = build.getStringValue("mcversion");
							String forgeversion = build.getStringValue("version");

							if (allowGui) {
								String forgeDir = String.format("%s-Forge%s%s", mcversion, forgeversion, branch);
								File forgeVersionDir = new File(minecraftDirectory, "versions/" + forgeDir);
								NwLogger.INSTALLER_LOGGER.fine("Searching for minecraftforge Installation at: " + forgeVersionDir.getAbsolutePath());
								if (forgeVersionDir.exists() && forgeVersionDir.isDirectory()) {
									File forgeVersionJson = new File(forgeVersionDir, forgeDir + ".json");
									if (forgeVersionJson.exists()) {
										int result = JOptionPane.showConfirmDialog(parentWindow, "A MinecraftForge Installation was found!\nDo you want to skip MinecraftForge Installation?", "MinecraftForge detected!", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
										if (result == JOptionPane.YES_OPTION) {
											return true;
										}
									}
								}
							}

							url = new URL(ModpackValues.URL_FORGE_INSTALLER + mcversion + "-" + forgeversion + branch + "/forge-" + mcversion + "-" + forgeversion + branch + "-installer.jar");
						}

						//Class loading
						NwLogger.INSTALLER_LOGGER.fine("Loading MC-Forge Installer...");
						URLClassLoader child = new URLClassLoader(new URL[]{url}, Installer.class.getClassLoader().getParent());
						Class forgeClientInstall = Class.forName("net.minecraftforge.installer.ClientInstall", true, child);
						Method runMethod = forgeClientInstall.getDeclaredMethod("run", File.class);
						Object instance = forgeClientInstall.newInstance();

						//Invoking Run Method
						NwLogger.INSTALLER_LOGGER.fine("Starting Client Installation...");
						Object result = runMethod.invoke(instance, minecraftDirectory);
						if ((Boolean) result) {
							NwLogger.INSTALLER_LOGGER.info("Minecraft Forge Installation finished.");
							return true;
						} else {
							NwLogger.INSTALLER_LOGGER.error("Minecraft Forge Installation has encountered an error!");
							return false;
						}
					} catch (ClassNotFoundException e) {
						NwLogger.INSTALLER_LOGGER.error("Error Loading Minecraft Forge Installer...", e);
					} catch (MalformedURLException e) {
						NwLogger.INSTALLER_LOGGER.error("Error parsing Minecraft Forge Installer version...", e);
					} catch (IOException e) {
						NwLogger.INSTALLER_LOGGER.error("Error reading Minecraft Forge Version Data", e);
					} catch (InvalidSyntaxException e) {
						NwLogger.INSTALLER_LOGGER.error("Error parsing Minecraft Forge Version Data", e);
					} catch (NumberFormatException e) {
						NwLogger.INSTALLER_LOGGER.error("Error parsing Minecraft Forge Build Number", e);
					} catch (IllegalArgumentException e) {
						NwLogger.INSTALLER_LOGGER.error("Error parsing Minecraft Forge Version Data", e);
					} catch (Exception e) {
						NwLogger.INSTALLER_LOGGER.error("Unknown Error occurred!", e);
					}
					return false;
				}
			} else {
				NwLogger.INSTALLER_LOGGER.info("Invalid Jar update type, falling back to direct download...");
				return DownloadHelper.downloadFile(repo.minecraft.versionName, file);
			}
		}
		return true;
	}

	/**
	 * create minecraft launcher profile <br>
	 * code is based on the MinecraftForge-Installer <br>
	 * will also update local modpack version, if file is found
	 *
	 * @return success of the profile creation
	 * @see <a href=https://github.com/MinecraftForge/Installer>https://github.com/MinecraftForge/Installer</a>
	 */
	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	public boolean createProfile(String profileName, String javaOptions, String gameDirectory, int updateFrequency) {
		if (createProfile) {
			File launcherProfiles = new File(minecraftDirectory, "launcher_profiles.json");
			if (!launcherProfiles.exists()) {
				JOptionPane.showMessageDialog(null, "The launcher_profiles.json file is missing!\nYou need to run the minecraft launcher at least once!", "File not found", JOptionPane.ERROR_MESSAGE);
				return false;
			}
			JdomParser parser = new JdomParser();
			JsonRootNode jsonProfileData;

			try {
				jsonProfileData = parser.parse(Files.newReader(launcherProfiles, Charsets.UTF_8));
			} catch (InvalidSyntaxException e) {
				JOptionPane.showMessageDialog(null, "The launcher profile file is corrupted. Re-run the minecraft launcher to fix it!", "Error", JOptionPane.ERROR_MESSAGE);
				return false;
			} catch (Exception e) {
				throw Throwables.propagate(e);
			}

			//our Data
			JsonField[] fields = new JsonField[]{
					JsonNodeFactories.field("name", JsonNodeFactories.string(profileName)),
					JsonNodeFactories.field("lastVersionId", JsonNodeFactories.string(this.name)),
					JsonNodeFactories.field("gameDir", JsonNodeFactories.string(gameDirectory)),
					JsonNodeFactories.field("javaArgs", JsonNodeFactories.string(javaOptions)),
			};

			HashMap<JsonStringNode, JsonNode> rootCopy = Maps.newHashMap(jsonProfileData.getFields());
			HashMap<JsonStringNode, JsonNode> profileCopy = Maps.newHashMap(jsonProfileData.getNode("profiles").getFields());


			JsonNode node = profileCopy.get(JsonNodeFactories.string(profileName));
			//keep data that we don't modify
			if (node != null && node.hasFields()) {
				List<JsonField> fieldList = node.getFieldList();
				Iterator<JsonField> iter = fieldList.iterator();
				while (iter.hasNext()) {
					JsonField element = iter.next();
					String text = element.getName().getText();
					if (text != null && !text.isEmpty()) {
						if (text.equals("name") || text.equals("lastVersionId") || text.equals("gameDir") || text.equals("javaArgs")) {
							iter.remove();
						}
					}
				}
				fieldList.addAll(Arrays.asList(fields));
				final JsonField[] fieldsArray = fieldList.toArray(new JsonField[fieldList.size()]);
				profileCopy.put(JsonNodeFactories.string(profileName), JsonNodeFactories.object(fieldsArray));
			} else {
				profileCopy.put(JsonNodeFactories.string(profileName), JsonNodeFactories.object(fields));
			}


			JsonRootNode profileJsonCopy = JsonNodeFactories.object(profileCopy);

			rootCopy.put(JsonNodeFactories.string("profiles"), profileJsonCopy);

			jsonProfileData = JsonNodeFactories.object(rootCopy);

			try {
				BufferedWriter newWriter = Files.newWriter(launcherProfiles, Charsets.UTF_8);
				PrettyJsonFormatter.fieldOrderPreservingPrettyJsonFormatter().format(jsonProfileData, newWriter);
				newWriter.close();
			} catch (Exception e) {
				JOptionPane.showMessageDialog(null, "There was a problem writing the launch profile,  is it write protected?", "Error", JOptionPane.ERROR_MESSAGE);
				return false;
			}

			//update version info
			File modpack = new File(gameDirectory, "modpack.json");
			//modpack data
			if (modpack.exists()) {
				try {
					Gson gson = new Gson();
					LocalModpack local = gson.fromJson(new FileReader(modpack),
							LocalModpack.class);
					local.version = repo.minecraft.version;
					FileWriter fileWriter = new FileWriter(modpack);
					gson.toJson(local, fileWriter);
					fileWriter.close();
				} catch (Exception e) {
					NwLogger.INSTALLER_LOGGER.warn("Could not read local modpack.json file of profile: " + profileName, e);
					JOptionPane.showMessageDialog(null, "Error when reading existing modpack.json file!\nInstalltion will continue...", "Warning!", JOptionPane.WARNING_MESSAGE);
				}
			}
			return true;
		} else {
			return true;
		}
	}

}
