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
package org.exoplatform.platform.am.cli

import org.exoplatform.platform.am.UnitTestsSpecification
import org.exoplatform.platform.am.ex.CommandLineParsingException
import org.exoplatform.platform.am.utils.Console
import spock.lang.Subject
import spock.lang.Unroll

import static org.exoplatform.platform.am.cli.CommandLineParameters.*

/**
 * Command line parameters parsing
 * @author Arnaud Héritier <aheritier@exoplatform.com>
 */
@Unroll
class CommandLineParserTest extends UnitTestsSpecification {
  private static final String validCatalogUrl = "http://somewhere.com/catalog"
  private static final String invalidCatalogUrl = "thisIsNotAnUrl"

  @Subject
  CommandLineParser clp = new CommandLineParser("FAKE", Console.get().width)

  def "Test usage"() {
    when:
    clp.usage()
    then:
    notThrown Throwable
  }

  def "Test command line parameters to display help with arguments : #args"(String[] args) {
    when:
    CommandLineParameters cliArgs = clp.parse(args)
    then:
    cliArgs.help
    where:
    args << [
        [HELP_SHORT_OPT],
        [HELP_LONG_OPT],
        [LIST_CMD, HELP_SHORT_OPT],
        [LIST_CMD, HELP_LONG_OPT],
        [DESCRIBE_CMD, HELP_SHORT_OPT],
        [DESCRIBE_CMD, HELP_LONG_OPT],
        [INSTALL_CMD, HELP_SHORT_OPT],
        [INSTALL_CMD, HELP_LONG_OPT],
        [UNINSTALL_CMD, HELP_SHORT_OPT],
        [UNINSTALL_CMD, HELP_LONG_OPT],
    ]
  }

  def "Test command line parameters to list all add-ons with arguments : #args"(String[] args) {
    when:
    CommandLineParameters cliArgs = clp.parse(args)
    then:
    CommandLineParameters.Command.LIST == cliArgs.command
    !cliArgs.commandList.catalog
    !cliArgs.commandList.installed
    !cliArgs.commandList.noCache
    !cliArgs.commandList.offline
    !cliArgs.commandList.outdated
    !cliArgs.commandList.snapshots
    !cliArgs.commandList.unstable
    !cliArgs.help
    !cliArgs.verbose
    !cliArgs.batchMode
    where:
    args << [
        [LIST_CMD],
    ]
  }

  def "Test command line parameters to list all add-ons without using cache with arguments : #args"(String[] args) {
    when:
    CommandLineParameters cliArgs = clp.parse(args)
    then:
    CommandLineParameters.Command.LIST == cliArgs.command
    !cliArgs.commandList.catalog
    !cliArgs.commandList.installed
    cliArgs.commandList.noCache
    !cliArgs.commandList.offline
    !cliArgs.commandList.outdated
    !cliArgs.commandList.snapshots
    !cliArgs.commandList.unstable
    !cliArgs.help
    !cliArgs.verbose
    !cliArgs.batchMode
    where:
    args << [
        [LIST_CMD, NO_CACHE_LONG_OPT],
    ]
  }

  def "Test command line parameters to list all add-ons while being offline with arguments : #args"(String[] args) {
    when:
    CommandLineParameters cliArgs = clp.parse(args)
    then:
    CommandLineParameters.Command.LIST == cliArgs.command
    !cliArgs.commandList.catalog
    !cliArgs.commandList.installed
    !cliArgs.commandList.noCache
    cliArgs.commandList.offline
    !cliArgs.commandList.outdated
    !cliArgs.commandList.snapshots
    !cliArgs.commandList.unstable
    !cliArgs.help
    !cliArgs.verbose
    !cliArgs.batchMode
    where:
    args << [
        [LIST_CMD, OFFLINE_LONG_OPT],
    ]
  }

  def "Test command line parameters to list all add-ons with verbose logs with arguments : #args"(String[] args) {
    when:
    CommandLineParameters cliArgs = clp.parse(args)
    then:
    CommandLineParameters.Command.LIST == cliArgs.command
    !cliArgs.commandList.catalog
    !cliArgs.commandList.installed
    !cliArgs.commandList.noCache
    !cliArgs.commandList.offline
    !cliArgs.commandList.outdated
    !cliArgs.commandList.snapshots
    !cliArgs.commandList.unstable
    !cliArgs.help
    cliArgs.verbose
    !cliArgs.batchMode
    where:
    args << [
        [LIST_CMD, VERBOSE_SHORT_OPT],
        [LIST_CMD, VERBOSE_LONG_OPT],
        [VERBOSE_SHORT_OPT, LIST_CMD],
        [VERBOSE_LONG_OPT, LIST_CMD],
    ]
  }

  def "Test command line parameters to list all add-ons including snapshots with arguments : #args"(String[] args) {
    when:
    CommandLineParameters cliArgs = clp.parse(args)
    then:
    CommandLineParameters.Command.LIST == cliArgs.command
    !cliArgs.commandList.catalog
    !cliArgs.commandList.installed
    !cliArgs.commandList.noCache
    !cliArgs.commandList.offline
    !cliArgs.commandList.outdated
    cliArgs.commandList.snapshots
    !cliArgs.commandList.unstable
    !cliArgs.help
    !cliArgs.verbose
    !cliArgs.batchMode
    where:
    args << [
        [LIST_CMD, SNAPSHOTS_LONG_OPT]
    ]
  }

  def "Test command line parameters to list all add-ons including snapshots with verbose logs with arguments : #args"(String[] args) {
    when:
    CommandLineParameters cliArgs = clp.parse(args)
    then:
    CommandLineParameters.Command.LIST == cliArgs.command
    !cliArgs.commandList.catalog
    !cliArgs.commandList.installed
    !cliArgs.commandList.noCache
    !cliArgs.commandList.offline
    !cliArgs.commandList.outdated
    cliArgs.commandList.snapshots
    !cliArgs.commandList.unstable
    !cliArgs.help
    cliArgs.verbose
    !cliArgs.batchMode
    where:
    args << [
        [LIST_CMD, VERBOSE_SHORT_OPT, SNAPSHOTS_LONG_OPT],
        [LIST_CMD, VERBOSE_LONG_OPT, SNAPSHOTS_LONG_OPT],
        [VERBOSE_SHORT_OPT, LIST_CMD, SNAPSHOTS_LONG_OPT],
        [VERBOSE_LONG_OPT, LIST_CMD, SNAPSHOTS_LONG_OPT]
    ]
  }

  def "Test command line parameters to list all add-ons including unstable with arguments : #args"(String[] args) {
    when:
    CommandLineParameters cliArgs = clp.parse(args)
    then:
    CommandLineParameters.Command.LIST == cliArgs.command
    !cliArgs.commandList.catalog
    !cliArgs.commandList.installed
    !cliArgs.commandList.noCache
    !cliArgs.commandList.offline
    !cliArgs.commandList.outdated
    !cliArgs.commandList.snapshots
    cliArgs.commandList.unstable
    !cliArgs.help
    !cliArgs.verbose
    !cliArgs.batchMode
    where:
    args << [
        [LIST_CMD, UNSTABLE_LONG_OPT]
    ]
  }

  def "Test command line parameters to list all add-ons including unstables with verbose logs with arguments : #args"(String[] args) {
    when:
    CommandLineParameters cliArgs = clp.parse(args)
    then:
    CommandLineParameters.Command.LIST == cliArgs.command
    !cliArgs.commandList.catalog
    !cliArgs.commandList.installed
    !cliArgs.commandList.noCache
    !cliArgs.commandList.offline
    !cliArgs.commandList.outdated
    !cliArgs.commandList.snapshots
    cliArgs.commandList.unstable
    !cliArgs.help
    cliArgs.verbose
    !cliArgs.batchMode
    where:
    args << [
        [LIST_CMD, VERBOSE_SHORT_OPT, UNSTABLE_LONG_OPT],
        [LIST_CMD, VERBOSE_LONG_OPT, UNSTABLE_LONG_OPT],
        [VERBOSE_SHORT_OPT, LIST_CMD, UNSTABLE_LONG_OPT],
        [VERBOSE_LONG_OPT, LIST_CMD, UNSTABLE_LONG_OPT]
    ]
  }

  def "Test command line parameters to list add-ons with a valid catalog parameter with arguments : #args"(String[] args) {
    when:
    CommandLineParameters cliArgs = clp.parse(args)
    then:
    CommandLineParameters.Command.LIST == cliArgs.command
    validCatalogUrl.equals(cliArgs.commandList.catalog.toString())
    !cliArgs.commandList.noCache
    !cliArgs.commandList.installed
    !cliArgs.commandList.offline
    !cliArgs.commandList.outdated
    !cliArgs.commandList.snapshots
    !cliArgs.commandList.unstable
    !cliArgs.help
    !cliArgs.verbose
    !cliArgs.batchMode
    where:
    args << [
        [LIST_CMD, "${CATALOG_LONG_OPT}=${validCatalogUrl}"],
    ]
  }

  def "Test command line parameters to list add-ons with a invalid catalog parameter with arguments : #args"(String[] args) {
    when:
    clp.parse(args)
    then:
    thrown(CommandLineParsingException)
    where:
    args << [
        [LIST_CMD, "${CATALOG_LONG_OPT}=${invalidCatalogUrl}"],
    ]
  }

  def "Test command line parameters to list installed add-ons with arguments : #args"(String[] args) {
    when:
    CommandLineParameters cliArgs = clp.parse(args)
    then:
    CommandLineParameters.Command.LIST == cliArgs.command
    !cliArgs.commandList.catalog
    cliArgs.commandList.installed
    !cliArgs.commandList.noCache
    !cliArgs.commandList.offline
    !cliArgs.commandList.outdated
    !cliArgs.commandList.snapshots
    !cliArgs.commandList.unstable
    !cliArgs.help
    !cliArgs.verbose
    !cliArgs.batchMode
    where:
    args << [
        [LIST_CMD, INSTALLED_LONG_OPT],
    ]
  }

  def "Test command line parameters to list installed add-ons with newest version(s) available with arguments : #args"(String[] args) {
    when:
    CommandLineParameters cliArgs = clp.parse(args)
    then:
    CommandLineParameters.Command.LIST == cliArgs.command
    !cliArgs.commandList.catalog
    !cliArgs.commandList.installed
    !cliArgs.commandList.noCache
    !cliArgs.commandList.offline
    cliArgs.commandList.outdated
    !cliArgs.commandList.snapshots
    !cliArgs.commandList.unstable
    !cliArgs.help
    !cliArgs.verbose
    !cliArgs.batchMode
    where:
    args << [
        [LIST_CMD, OUTDATED_LONG_OPT],
    ]
  }

  def "Test command line parameters to describe the latest version of an add-on with arguments : #args"(String[] args) {
    when:
    CommandLineParameters cliArgs = clp.parse(args)
    then:
    CommandLineParameters.Command.DESCRIBE == cliArgs.command
    "my-addon".equals(cliArgs.commandDescribe.addonId)
    !cliArgs.commandDescribe.addonVersion
    !cliArgs.commandDescribe.catalog
    !cliArgs.commandDescribe.noCache
    !cliArgs.commandDescribe.offline
    !cliArgs.help
    !cliArgs.verbose
    !cliArgs.batchMode
    where:
    args << [
        [DESCRIBE_CMD, "my-addon"],
    ]
  }

  def "Test command line parameters to describe with too much params with arguments : #args"(String[] args) {
    when:
    clp.parse(args)
    then:
    thrown CommandLineParsingException
    where:
    args << [
        [DESCRIBE_CMD, "foo", "bar"],
    ]
  }

  def "Test command line parameters to describe the latest version of an add-on without using cache with arguments : #args"(String[] args) {
    when:
    CommandLineParameters cliArgs = clp.parse(args)
    then:
    CommandLineParameters.Command.DESCRIBE == cliArgs.command
    "my-addon".equals(cliArgs.commandDescribe.addonId)
    !cliArgs.commandDescribe.addonVersion
    !cliArgs.commandDescribe.catalog
    cliArgs.commandDescribe.noCache
    !cliArgs.commandDescribe.offline
    !cliArgs.help
    !cliArgs.verbose
    !cliArgs.batchMode
    where:
    args << [
        [DESCRIBE_CMD, "my-addon", NO_CACHE_LONG_OPT],
    ]
  }

  def "Test command line parameters to describe the latest version of an add-on while being offline with arguments : #args"(String[] args) {
    when:
    CommandLineParameters cliArgs = clp.parse(args)
    then:
    CommandLineParameters.Command.DESCRIBE == cliArgs.command
    "my-addon".equals(cliArgs.commandDescribe.addonId)
    !cliArgs.commandDescribe.addonVersion
    !cliArgs.commandDescribe.catalog
    !cliArgs.commandDescribe.noCache
    cliArgs.commandDescribe.offline
    !cliArgs.help
    !cliArgs.verbose
    !cliArgs.batchMode
    where:
    args << [
        [DESCRIBE_CMD, "my-addon", OFFLINE_LONG_OPT],
    ]
  }

  def "Test command line parameters to describe a given version of an add-on with arguments : #args"(String[] args) {
    when:
    CommandLineParameters cliArgs = clp.parse(args)
    then:
    "my-addon".equals(cliArgs.commandDescribe.addonId)
    "42".equals(cliArgs.commandDescribe.addonVersion)
    !cliArgs.commandDescribe.catalog
    !cliArgs.commandDescribe.noCache
    !cliArgs.commandDescribe.offline
    !cliArgs.help
    !cliArgs.verbose
    !cliArgs.batchMode
    where:
    args << [
        [DESCRIBE_CMD, "my-addon:42"],
    ]
  }

  def "Test command line parameters to describe an add-on with a valid catalog parameter with arguments : #args"(String[] args) {
    when:
    CommandLineParameters cliArgs = clp.parse(args)
    then:
    CommandLineParameters.Command.DESCRIBE == cliArgs.command
    "my-addon".equals(cliArgs.commandDescribe.addonId)
    validCatalogUrl.equals(cliArgs.commandDescribe.catalog.toString())
    !cliArgs.commandDescribe.noCache
    !cliArgs.commandDescribe.offline
    !cliArgs.help
    !cliArgs.verbose
    !cliArgs.batchMode
    where:
    args << [
        [DESCRIBE_CMD, "my-addon", "${CATALOG_LONG_OPT}=${validCatalogUrl}"],
        [DESCRIBE_CMD, "my-addon:42", "${CATALOG_LONG_OPT}=${validCatalogUrl}"],
    ]
  }

  def "Test command line parameters to describe an add-on with a invalid catalog parameter with arguments : #args"(String[] args) {
    when:
    clp.parse(args)
    then:
    thrown(CommandLineParsingException)
    where:
    args << [
        [DESCRIBE_CMD, "my-addon", "${CATALOG_LONG_OPT}=${invalidCatalogUrl}"],
        [DESCRIBE_CMD, "my-addon:42", "${CATALOG_LONG_OPT}=${invalidCatalogUrl}"],
    ]
  }

  def "Test command line parameters to install the latest version of an add-on with arguments : #args"(String[] args) {
    when:
    CommandLineParameters cliArgs = clp.parse(args)
    then:
    CommandLineParameters.Command.INSTALL == cliArgs.command
    "my-addon".equals(cliArgs.commandInstall.addonId)
    !cliArgs.commandInstall.addonVersion
    !cliArgs.commandInstall.catalog
    cliArgs.commandInstall.conflict == Conflict.FAIL
    !cliArgs.commandInstall.force
    !cliArgs.commandInstall.noCache
    !cliArgs.commandInstall.noCompat
    !cliArgs.commandInstall.offline
    !cliArgs.commandInstall.snapshots
    !cliArgs.commandInstall.unstable
    !cliArgs.help
    !cliArgs.verbose
    !cliArgs.batchMode
    where:
    args << [
        [INSTALL_CMD, "my-addon"],
    ]
  }

  def "Test command line parameters to install the latest version of an add-on in non-interactive (batch) mode with arguments : #args"(String[] args) {
    when:
    CommandLineParameters cliArgs = clp.parse(args)
    then:
    CommandLineParameters.Command.INSTALL == cliArgs.command
    "my-addon".equals(cliArgs.commandInstall.addonId)
    !cliArgs.commandInstall.addonVersion
    !cliArgs.commandInstall.catalog
    cliArgs.commandInstall.conflict == Conflict.FAIL
    !cliArgs.commandInstall.force
    !cliArgs.commandInstall.noCache
    !cliArgs.commandInstall.noCompat
    !cliArgs.commandInstall.offline
    !cliArgs.commandInstall.snapshots
    !cliArgs.commandInstall.unstable
    !cliArgs.help
    !cliArgs.verbose
    cliArgs.batchMode
    where:
    args << [
        [INSTALL_CMD, "my-addon", BATCH_SHORT_OPT],
        [INSTALL_CMD, "my-addon", BATCH_LONG_OPT],
        [BATCH_SHORT_OPT, INSTALL_CMD, "my-addon"],
        [BATCH_LONG_OPT, INSTALL_CMD, "my-addon"],
    ]
  }

  def "Test command line parameters to install the latest version of an add-on without using cache with arguments : #args"(String[] args) {
    when:
    CommandLineParameters cliArgs = clp.parse(args)
    then:
    CommandLineParameters.Command.INSTALL == cliArgs.command
    "my-addon".equals(cliArgs.commandInstall.addonId)
    !cliArgs.commandInstall.addonVersion
    !cliArgs.commandInstall.catalog
    cliArgs.commandInstall.conflict == Conflict.FAIL
    !cliArgs.commandInstall.force
    cliArgs.commandInstall.noCache
    !cliArgs.commandInstall.noCompat
    !cliArgs.commandInstall.offline
    !cliArgs.commandInstall.snapshots
    !cliArgs.commandInstall.unstable
    !cliArgs.help
    !cliArgs.verbose
    !cliArgs.batchMode
    where:
    args << [
        [INSTALL_CMD, "my-addon", "--no-cache"],
    ]
  }

  def "Test command line parameters to install the latest version of an add-on while being offline with arguments : #args"(String[] args) {
    when:
    CommandLineParameters cliArgs = clp.parse(args)
    then:
    CommandLineParameters.Command.INSTALL == cliArgs.command
    "my-addon".equals(cliArgs.commandInstall.addonId)
    !cliArgs.commandInstall.addonVersion
    !cliArgs.commandInstall.catalog
    cliArgs.commandInstall.conflict == Conflict.FAIL
    !cliArgs.commandInstall.force
    !cliArgs.commandInstall.noCache
    !cliArgs.commandInstall.noCompat
    cliArgs.commandInstall.offline
    !cliArgs.commandInstall.snapshots
    !cliArgs.commandInstall.unstable
    !cliArgs.help
    !cliArgs.verbose
    !cliArgs.batchMode
    where:
    args << [
        [INSTALL_CMD, "my-addon", "--offline"],
    ]
  }

  def "Test command line parameters to install a given version of an add-on with arguments : #args"(String[] args) {
    when:
    CommandLineParameters cliArgs = clp.parse(args)
    then:
    "my-addon".equals(cliArgs.commandInstall.addonId)
    "42".equals(cliArgs.commandInstall.addonVersion)
    !cliArgs.commandInstall.catalog
    cliArgs.commandInstall.conflict == Conflict.FAIL
    !cliArgs.commandInstall.force
    !cliArgs.commandInstall.noCache
    !cliArgs.commandInstall.noCompat
    !cliArgs.commandInstall.offline
    !cliArgs.commandInstall.snapshots
    !cliArgs.commandInstall.unstable
    !cliArgs.help
    !cliArgs.verbose
    !cliArgs.batchMode
    where:
    args << [
        [INSTALL_CMD, "my-addon:42"],
    ]
  }

  def "Test command line parameters to force to install an add-on with arguments : #args"(String[] args) {
    when:
    CommandLineParameters cliArgs = clp.parse(args)
    then:
    CommandLineParameters.Command.INSTALL == cliArgs.command
    "my-addon".equals(cliArgs.commandInstall.addonId)
    !cliArgs.commandInstall.catalog
    cliArgs.commandInstall.conflict == Conflict.FAIL
    cliArgs.commandInstall.force
    !cliArgs.commandInstall.noCache
    !cliArgs.commandInstall.noCompat
    !cliArgs.commandInstall.offline
    !cliArgs.commandInstall.snapshots
    !cliArgs.commandInstall.unstable
    !cliArgs.help
    !cliArgs.verbose
    !cliArgs.batchMode
    where:
    args << [
        [INSTALL_CMD, "my-addon", FORCE_LONG_OPT],
        [INSTALL_CMD, "my-addon:42", FORCE_LONG_OPT],
    ]
  }

  def "Test command line parameters to install a SNAPSHOT version of an add-on with arguments : #args"(String[] args) {
    when:
    CommandLineParameters cliArgs = clp.parse(args)
    then:
    CommandLineParameters.Command.INSTALL == cliArgs.command
    "my-addon".equals(cliArgs.commandInstall.addonId)
    !cliArgs.commandInstall.catalog
    cliArgs.commandInstall.conflict == Conflict.FAIL
    !cliArgs.commandInstall.force
    !cliArgs.commandInstall.noCache
    !cliArgs.commandInstall.noCompat
    !cliArgs.commandInstall.offline
    cliArgs.commandInstall.snapshots
    !cliArgs.commandInstall.unstable
    !cliArgs.help
    !cliArgs.verbose
    !cliArgs.batchMode
    where:
    args << [
        [INSTALL_CMD, "my-addon", SNAPSHOTS_LONG_OPT],
        [INSTALL_CMD, "my-addon:42-SNAPSHOT", SNAPSHOTS_LONG_OPT],
    ]
  }

  def "Test command line parameters to install an unstable version of an add-on with arguments : #args"(String[] args) {
    when:
    CommandLineParameters cliArgs = clp.parse(args)
    then:
    CommandLineParameters.Command.INSTALL == cliArgs.command
    "my-addon".equals(cliArgs.commandInstall.addonId)
    !cliArgs.commandInstall.catalog
    cliArgs.commandInstall.conflict == Conflict.FAIL
    !cliArgs.commandInstall.force
    !cliArgs.commandInstall.noCache
    !cliArgs.commandInstall.noCompat
    !cliArgs.commandInstall.offline
    !cliArgs.commandInstall.snapshots
    cliArgs.commandInstall.unstable
    !cliArgs.help
    !cliArgs.verbose
    !cliArgs.batchMode
    where:
    args << [
        [INSTALL_CMD, "my-addon", UNSTABLE_LONG_OPT],
        [INSTALL_CMD, "my-addon:42-RC1", UNSTABLE_LONG_OPT],
    ]
  }

  def "Test command line parameters to install an add-on without compatibility checks with arguments : #args"(String[] args) {
    when:
    CommandLineParameters cliArgs = clp.parse(args)
    then:
    CommandLineParameters.Command.INSTALL == cliArgs.command
    "my-addon".equals(cliArgs.commandInstall.addonId)
    !cliArgs.commandInstall.catalog
    cliArgs.commandInstall.conflict == Conflict.FAIL
    !cliArgs.commandInstall.force
    !cliArgs.commandInstall.noCache
    cliArgs.commandInstall.noCompat
    !cliArgs.commandInstall.offline
    !cliArgs.commandInstall.snapshots
    !cliArgs.commandInstall.unstable
    !cliArgs.help
    !cliArgs.verbose
    !cliArgs.batchMode
    where:
    args << [
        [INSTALL_CMD, "my-addon", NO_COMPAT_LONG_OPT],
    ]
  }

  def "Test command line parameters to install an add-on overriding any existing file with arguments : #args"(String[] args) {
    when:
    CommandLineParameters cliArgs = clp.parse(args)
    then:
    CommandLineParameters.Command.INSTALL == cliArgs.command
    "my-addon".equals(cliArgs.commandInstall.addonId)
    !cliArgs.commandInstall.catalog
    cliArgs.commandInstall.conflict == Conflict.OVERWRITE
    !cliArgs.commandInstall.force
    !cliArgs.commandInstall.noCache
    !cliArgs.commandInstall.noCompat
    !cliArgs.commandInstall.offline
    !cliArgs.commandInstall.snapshots
    !cliArgs.commandInstall.unstable
    !cliArgs.help
    !cliArgs.verbose
    !cliArgs.batchMode
    where:
    args << [
        [INSTALL_CMD, "my-addon", "${CONFLICT_LONG_OPT}=overwrite"],
    ]
  }

  def "Test command line parameters to install an add-on skipping any existing file with arguments : #args"(String[] args) {
    when:
    CommandLineParameters cliArgs = clp.parse(args)
    then:
    CommandLineParameters.Command.INSTALL == cliArgs.command
    "my-addon".equals(cliArgs.commandInstall.addonId)
    !cliArgs.commandInstall.catalog
    cliArgs.commandInstall.conflict == Conflict.SKIP
    !cliArgs.commandInstall.force
    !cliArgs.commandInstall.noCache
    !cliArgs.commandInstall.noCompat
    !cliArgs.commandInstall.offline
    !cliArgs.commandInstall.snapshots
    !cliArgs.commandInstall.unstable
    !cliArgs.help
    !cliArgs.verbose
    !cliArgs.batchMode
    where:
    args << [
        [INSTALL_CMD, "my-addon", "${CONFLICT_LONG_OPT}=skip"],
    ]
  }


  def "Test command line parameters to install an add-on with invalid conflict parameter with arguments : #args"(String[] args) {
    when:
    CommandLineParameters cliArgs = clp.parse(args)
    then:
    thrown(CommandLineParsingException)
    where:
    args << [
        [INSTALL_CMD, "my-addon", "${CONFLICT_LONG_OPT}=foo"],
    ]
  }

  def "Test command line parameters to install an add-on with a valid catalog parameter with arguments : #args"(String[] args) {
    when:
    CommandLineParameters cliArgs = clp.parse(args)
    then:
    CommandLineParameters.Command.INSTALL == cliArgs.command
    "my-addon".equals(cliArgs.commandInstall.addonId)
    validCatalogUrl.equals(cliArgs.commandInstall.catalog.toString())
    cliArgs.commandInstall.conflict == Conflict.FAIL
    !cliArgs.commandInstall.force
    !cliArgs.commandInstall.noCache
    !cliArgs.commandInstall.noCompat
    !cliArgs.commandInstall.offline
    !cliArgs.commandInstall.snapshots
    !cliArgs.commandInstall.unstable
    !cliArgs.help
    !cliArgs.verbose
    !cliArgs.batchMode
    where:
    args << [
        [INSTALL_CMD, "my-addon", "${CATALOG_LONG_OPT}=${validCatalogUrl}"],
        [INSTALL_CMD, "my-addon:42", "${CATALOG_LONG_OPT}=${validCatalogUrl}"],
    ]
  }

  def "Test command line parameters to install an add-on with a invalid catalog parameter with arguments : #args"(String[] args) {
    when:
    clp.parse(args)
    then:
    thrown(CommandLineParsingException)
    where:
    args << [
        [INSTALL_CMD, "my-addon", "${CATALOG_LONG_OPT}=${invalidCatalogUrl}"],
        [INSTALL_CMD, "my-addon:42", "${CATALOG_LONG_OPT}=${invalidCatalogUrl}"],
    ]
  }

  def "Test command line parameters to uninstall an add-on with arguments : #args"(String[] args) {
    when:
    CommandLineParameters cliArgs = clp.parse(args)
    then:
    CommandLineParameters.Command.UNINSTALL == cliArgs.command
    "my-addon".equals(cliArgs.commandUninstall.addonId)
    !cliArgs.help
    !cliArgs.verbose
    !cliArgs.batchMode
    where:
    args << [
        [UNINSTALL_CMD, "my-addon"],
    ]
  }

  def "Test command line parameters to uninstall an add-on with a version with arguments : #args"(String[] args) {
    when:
    CommandLineParameters cliArgs = clp.parse(args)
    then:
    CommandLineParameters.Command.UNINSTALL == cliArgs.command
    "my-addon".equals(cliArgs.commandUninstall.addonId)
    !cliArgs.help
    !cliArgs.verbose
    !cliArgs.batchMode
    where:
    args << [
        [UNINSTALL_CMD, "my-addon:42"],
    ]
  }

  def "Test invalid command line parameters with arguments : #args"(String[] args) {
    when:
    clp.parse(args)
    then:
    thrown(CommandLineParsingException)
    where:
    args << [
        []
        // Missing params
        [INSTALL_CMD],
        [UNINSTALL_CMD],
        // Too much params
        [INSTALL_CMD, "foo", "bar"],
        [INSTALL_CMD, "foo", "${CATALOG_LONG_OPT}=http://firstURL.com", "${CATALOG_LONG_OPT}=http://secondURL.com"],
        [UNINSTALL_CMD, "foo", "bar"],
    ]
  }

}
