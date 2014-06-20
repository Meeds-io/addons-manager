/*
 * Copyright (C) 2003-2014 eXo Platform SAS.
 *
 * This file is part of eXo Platform - Add-ons Manager.
 *
 * eXo Platform - Add-ons Manager is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * eXo Platform - Add-ons Manager software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with eXo Platform - Add-ons Manager; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.platform.am

/**
 * @author Arnaud Héritier <aheritier@exoplatform.com>
 */
class CatalogService {
  List<Addon> mergeCatalogs(final List<Addon> centralCatalog, final List<Addon> localCatalog) {
    // [AM_CAT_07] At merge, de-duplication of add-on entries of the local and remote catalogs is
    // done using ID, Version, Distributions, Application Servers as the identifier.
    // In case of duplication, the remote entry takes precedence
    List<Addon> mergedCatalog = centralCatalog.clone()
    localCatalog.findAll { !centralCatalog.contains(it) }.each { mergedCatalog.add(it) }
    return mergedCatalog
  }
}
