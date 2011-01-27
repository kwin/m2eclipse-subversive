/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.sonatype.m2e.subversive.tests;

import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.sonatype.m2e.subversive.internal.SubversiveScmHandler;


/**
 * Tests the proper integration of the Subversive SCM handler.
 */
@SuppressWarnings("restriction")
public class SubversiveScmHandlerTest extends AbstractScmHandlerTest {

  private static final String SCM_SVN_PATH = "resources/svn/";

  private SvnServer svnServer;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    SubversiveTestUtils.cleanSVNTeamPluginConfig();
  }

  @Override
  public void tearDown() throws Exception {
    try {
      if(svnServer != null) {
        svnServer.stop();
        svnServer = null;
      }
      SubversiveTestUtils.cleanSVNTeamPluginConfig();
    } finally {
      super.tearDown();
    }
  }

  private SvnServer startSvnServer(String dump, String config) throws Exception {
    svnServer = new SvnServer();
    svnServer.setDumpFile(SCM_SVN_PATH + dump);
    svnServer.setConfDir(SCM_SVN_PATH + config);
    svnServer.start();
    return svnServer;
  }

  public void testCheckout() throws Exception {
    startSvnServer("simple.dump", "conf-a");

    checkout(SubversiveScmHandler.SVN_SCM_ID + svnServer.getUrl() + "/simple/trunk");
    waitForJobsToComplete();
    SubversiveTestUtils.waitForTeamSharingJobs();

    assertWorkspaceProjectShared("svn-test");

    IRepositoryLocation location = SVNRemoteStorage.instance().getRepositoryLocation(getWorkspaceProject("svn-test"));
    assertEquals(svnServer.getUrl() + "/simple", location.getUrl());
  }

}
