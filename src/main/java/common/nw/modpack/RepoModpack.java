package common.nw.modpack;

import java.util.List;

public class RepoModpack {

	public String modpackName;

	public int updaterRevision;

	public RepoVersionInfo minecraft;

	public String modpackRepo;

	public List<RepoMod> blacklist;
	public List<RepoMod> files;
}
