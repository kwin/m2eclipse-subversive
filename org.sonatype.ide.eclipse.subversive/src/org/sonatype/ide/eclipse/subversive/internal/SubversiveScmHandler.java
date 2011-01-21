/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.sonatype.ide.eclipse.subversive.internal;

import java.io.File;
import java.net.MalformedURLException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.team.svn.core.connector.ISVNConnector.Depth;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.file.CheckoutAsOperation;
import org.eclipse.team.svn.core.operation.remote.management.AddRepositoryLocationOperation;
import org.eclipse.team.svn.core.operation.remote.management.SaveRepositoryLocationsOperation;
import org.eclipse.team.svn.core.resource.IRepositoryContainer;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.utility.ILoggedOperationFactory;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.maven.ide.eclipse.project.MavenProjectScmInfo;
import org.maven.ide.eclipse.scm.ScmHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A SCM handler to enable M2Eclipse to checkout using Subversive.
 */
public class SubversiveScmHandler extends ScmHandler {

  private static final Logger log = LoggerFactory.getLogger(SubversiveScmHandler.class);

  public static final String SVN_SCM_ID = "scm:svn:";

  @Override
  public void checkoutProject(MavenProjectScmInfo info, File location, IProgressMonitor monitor) throws CoreException,
      InterruptedException {
    log.debug("Checking out project from {} to {}", info, location);

    enterCheckout(info, location);
    try {
      IRepositoryContainer container = getSVNRepositoryContainer(info);

      prepareCheckout(info, location, container);

      CheckoutAsOperation checkout = new CheckoutAsOperation(location, container, Depth.INFINITY, false, true);

      AddRepositoryLocationOperation add = new AddRepositoryLocationOperation(container.getRepositoryLocation());

      SaveRepositoryLocationsOperation save = new SaveRepositoryLocationsOperation();

      CompositeOperation op = new CompositeOperation(checkout.getOperationName(), checkout.getMessagesClass());
      op.add(checkout);
      op.add(add, new IActionOperation[] {checkout});
      op.add(save, new IActionOperation[] {add});

      ProgressMonitorUtility.doTaskExternal(op, monitor, ILoggedOperationFactory.EMPTY);

      IStatus status = op.getStatus();
      if(status != null && !status.isOK()) {
        throw new CoreException(status);
      }
    } finally {
      leaveCheckout();
    }
  }

  protected void enterCheckout(MavenProjectScmInfo info, File location) throws CoreException {
    // extension hook
  }

  protected void prepareCheckout(MavenProjectScmInfo info, File location, IRepositoryContainer container)
      throws CoreException {
    // extension hook
  }

  protected void leaveCheckout() {
    // extension hook
  }

  private IRepositoryContainer getSVNRepositoryContainer(MavenProjectScmInfo info) throws CoreException {
    String url = info.getFolderUrl().substring(SVN_SCM_ID.length());

    // Force svn to verify the url
    try {
      SVNUtility.getSVNUrl(url);
    } catch(MalformedURLException e) {
      throw new CoreException(new Status(IStatus.ERROR, "SubversiveScmHandler", 0, "Invalid url "
          + info.getFolderUrl().substring(SVN_SCM_ID.length()), e));
    }

    IRepositoryContainer container = (IRepositoryContainer) SVNUtility.asRepositoryResource(url, true);
    container.setSelectedRevision(SVNRevision.fromString(info.getRevision()));
    IRepositoryLocation repositoryLocation = container.getRepositoryLocation();

    fixRepositoryUrl(repositoryLocation);

    return container;
  }

  private void fixRepositoryUrl(IRepositoryLocation location) {
    /*
     * Workaround for bug in SVNUtility.initializeRepositoryLocation() which collapses all double slashes on
     * non-Windows platforms, thereby screwing the scheme part of the URL (https://bugs.eclipse.org/bugs/show_bug.cgi?id=303085).
     */
    String url = location.getUrlAsIs();
    int idx = url.indexOf(":/");
    if(idx > 0 && !url.substring(idx).startsWith("://")) {
      url = url.substring(0, idx) + "://" + url.substring(idx + 2);
      location.setUrl(url);
    }
  }

}
