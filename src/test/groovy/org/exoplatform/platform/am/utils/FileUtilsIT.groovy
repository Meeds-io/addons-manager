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
package org.exoplatform.platform.am.utils
import org.exoplatform.platform.am.IntegrationTestsSpecification
import spock.lang.Subject
/**
 * @author Arnaud Héritier <aheritier@exoplatform.com>
 */
@Subject(FileUtils)
class FileUtilsIT extends IntegrationTestsSpecification {

  def "[AM_CAT_01] The download mechanism must follow permanent redirects"() {
    setup:
    def originalFile = new File(getTestDataDir(), "catalog.json")
    def downloadedFile = File.createTempFile("test", ".json")
    when:
    FileUtils.downloadFile("Testing a download with permanent redirection (301)","${getWebServerRootUrl()}/catalog-redirect-301.jsp", downloadedFile)
    then:
    originalFile.text.equals(downloadedFile.text)
    cleanup:
    downloadedFile.delete()
  }

  def "[AM_CAT_01] The download mechanism must follow temporary redirects"() {
    setup:
    def originalFile = new File(getTestDataDir(), "catalog.json")
    def downloadedFile = File.createTempFile("test", ".json")
    when:
    FileUtils.downloadFile("Testing a download with a temporary redirection (302)","${getWebServerRootUrl()}/catalog-redirect-302.jsp", downloadedFile)
    then:
    originalFile.text.equals(downloadedFile.text)
    cleanup:
    downloadedFile.delete()
  }

}
