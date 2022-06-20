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
package org.exoplatform.platform.am.settings

import org.exoplatform.platform.am.IntegrationTestsSpecification
import spock.lang.Subject

@Subject(PlatformSettings)
class PlatformSettingsIT extends IntegrationTestsSpecification {

  def "validate Settings"() {
    expect:
    getPlatformSettings()
  }

  def "validate Version"() {
    setup:
    def expectedVersion =
        getPlatformHome().name.replaceAll("platform-", "").replaceAll("community-", "").replaceAll("-jboss-standalone", "")
    expect:
    expectedVersion == getPlatformSettings().version
  }

  def "Validate AppServer Type"() {
    setup:
    def expectedAppServerType =
        getPlatformHome().name.contains("jboss") ?
            PlatformSettings.AppServerType.JBOSS : PlatformSettings.AppServerType.TOMCAT
    expect:
    expectedAppServerType == getPlatformSettings().appServerType
  }

  def "Validate Distribution Type"() {
    setup:
    def expectedDistributionType
    if(getPlatformHome().name.contains("exo-platform-community")) {
      expectedDistributionType = PlatformSettings.DistributionType.EXO_COMMUNITY
    } else if (getPlatformHome().name.contains("community")) {
      expectedDistributionType = PlatformSettings.DistributionType.COMMUNITY
    } else if (getPlatformHome().name.contains("enterprise")) {
      expectedDistributionType = PlatformSettings.DistributionType.ENTERPRISE
    } else {
      expectedDistributionType = PlatformSettings.DistributionType.UNKNOWN
    }
    expect:
    expectedDistributionType == getPlatformSettings().distributionType
  }
}
