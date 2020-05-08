/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 Meeds Association
 * contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.exoplatform.platform.am

import org.exoplatform.platform.am.utils.Logger
import spock.lang.Specification

/**
 * This class offers various helpers methods to write unit tests
 * @author Arnaud Héritier <aheritier@exoplatform.com>
 */
abstract class UnitTestsSpecification extends Specification {

  public static final String UT_SYSPROP_VERBOSE = "ut.verbose"

  /**
   * @return true if verbose mode must be used while executing tests
   */
  boolean isVerbose() {
    Boolean.getBoolean(UT_SYSPROP_VERBOSE)
  }

  def setup() {
    if (verbose) {
      Logger.getInstance().enableDebug()
    } else {
      Logger.getInstance().disableDebug()
    }
  }

  def cleanup() {
    Logger.getInstance().disableDebug()
  }

}