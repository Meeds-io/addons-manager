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
package org.exoplatform.platform.am.settings

import java.io.FileInputStream;

import org.exoplatform.platform.am.ex.ErroneousSetupException
import org.exoplatform.platform.am.utils.Logger
/**
 * This class stores the add-ons manager settings
 * @author Arnaud Héritier <aheritier@exoplatform.com>
 */
class AddonsManagerSettings extends Properties {
  /**
   * Logger
   */
  private static final Logger LOG = Logger.getInstance()

  static final String ADDONS_MANAGER_PROPERTIES_JVM_PROPERTY = "addons-manager.properties.path"
  static final String ADDONS_MANAGER_PROPERTIES = "am.properties"
  static final String PROPERTY_PREFIX = "am"

  AddonsManagerSettings() {
    super()
    init()
  }

  /**
   * Automatically load properties for {@link AddonsManagerSettings#ADDONS_MANAGER_PROPERTIES}
   */
  protected void init() {
    InputStream inputStream = null;
    if (System.getProperty("addons-manager.properties.path") != null) {
      inputStream = new FileInputStream(System.getProperty("addons-manager.properties.path"))
    } else {
      inputStream = getClass().getClassLoader().getResourceAsStream(ADDONS_MANAGER_PROPERTIES)
    }

    if (inputStream == null) {
      throw new ErroneousSetupException(
          "Erroneous packaging, Property file \"${ADDONS_MANAGER_PROPERTIES}\" not found in the classpath")
    }
    try {
      load(inputStream)
    } catch (IOException ioe) {
      throw new ErroneousSetupException("Error while reading \"${ADDONS_MANAGER_PROPERTIES}\" : ${ioe.message}", ioe)
    } finally {
      try {
        inputStream.close()
      } catch (IOException ioe) {
        LOG.warn("Error while closing \"${ADDONS_MANAGER_PROPERTIES}\" : ${ioe.message}")
      }
    }
    // Computes the script name from the OS
    if (System.properties['os.name'].toLowerCase().contains('windows')) {
      setProperty("scriptName", "${scriptBaseName}.bat")
    } else {
      setProperty("scriptName", "${scriptBaseName}")
    }
  }

  /**
   * Allows to override any property loaded from {@link AddonsManagerSettings#ADDONS_MANAGER_PROPERTIES}
   * with a system property {@link AddonsManagerSettings#PROPERTY_PREFIX}.XXX where XXX is the property key
   */
  @Override
  Object get(Object key) {
    return System.getProperty("${PROPERTY_PREFIX}.${key}") ? System.getProperty("am.${key}") : super.getProperty(key)
  }
}
