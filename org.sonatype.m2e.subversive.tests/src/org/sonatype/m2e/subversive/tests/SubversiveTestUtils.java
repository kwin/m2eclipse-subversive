/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.sonatype.m2e.subversive.tests;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.team.svn.core.SVNTeamPlugin;
import org.eclipse.team.svn.core.operation.file.SVNFileStorage;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.svnstorage.AbstractSVNStorage;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;


public class SubversiveTestUtils {

  public static void cleanSVNTeamPluginConfig() throws Exception {
    removeSvnLocations(SVNRemoteStorage.instance());
    removeSvnLocations(SVNFileStorage.instance());
  }

  public static void removeSvnLocations(AbstractSVNStorage svnStorage) throws Exception {
    IRepositoryLocation[] locations = svnStorage.getRepositoryLocations();
    if(locations == null || locations.length == 0) {
      return;
    }
    for(IRepositoryLocation location : locations) {
      svnStorage.removeRepositoryLocation(location);
    }
    SVNTeamPlugin.instance().setLocationsDirty(true);
    svnStorage.saveConfiguration();
  }

  public static void waitForTeamSharingJobs() {
    /*
     * NOTE: Project sharing happens by jobs and we better wait for these jobs to finish before we assert any
     * project is shared. In the case of Subversive, the additional issue is that its "Reconnect project" job would
     * fail with a blocking UI error dialog in case it gets run after the test has been teared down and cleaned the
     * workspace.
     */
    waitForJobs("(.*\\.AutoBuild.*)" + "|(org\\.eclipse\\.team\\.svn\\.core.*)", 10000);
  }

  private static void waitForJobs(String classNameRegex, int maxWaitMillis) {
    final int waitMillis = 250;
    for(int i = maxWaitMillis / waitMillis; i >= 0; i-- ) {
      if(!hasJob(classNameRegex)) {
        return;
      }
      try {
        Thread.sleep(waitMillis);
      } catch(InterruptedException e) {
        // ignore
      }
    }
  }

  private static boolean hasJob(String classNameRegex) {
    Job[] jobs = Job.getJobManager().find(null);
    for(Job job : jobs) {
      if(job.getClass().getName().matches(classNameRegex)) {
        return true;
      }
    }
    return false;
  }

}
