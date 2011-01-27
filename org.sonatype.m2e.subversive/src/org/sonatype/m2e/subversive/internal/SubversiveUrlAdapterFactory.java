/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.sonatype.m2e.subversive.internal;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.m2e.core.scm.ScmUrl;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.ui.repository.model.IResourceTreeNode;


/**
 * An adapter factory that enables M2Eclipse to checkout Maven projects from Subversive's repository view.
 */
public class SubversiveUrlAdapterFactory implements IAdapterFactory {

  private static final Class<?>[] LIST = {ScmUrl.class};

  public Class<?>[] getAdapterList() {
    return LIST;
  }

  @SuppressWarnings("rawtypes")
  public Object getAdapter(Object adaptableObject, Class adapterType) {
    if(ScmUrl.class.equals(adapterType) && (adaptableObject instanceof IResourceTreeNode)) {
      IRepositoryResource repositoryResource = ((IResourceTreeNode) adaptableObject).getRepositoryResource();
      String scmUrl = SubversiveScmHandler.SVN_SCM_ID + repositoryResource.getUrl();
      String scmParentUrl = null;
      IRepositoryResource parent = repositoryResource.getParent();
      if(parent != null) {
        scmParentUrl = SubversiveScmHandler.SVN_SCM_ID + parent.getUrl();
      }
      return new ScmUrl(scmUrl, scmParentUrl);
    }
    return null;
  }

}
