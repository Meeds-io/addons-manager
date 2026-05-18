add-ons manager
==============

## DESCRIPTION

Command line tool to install/uninstall add-ons

## LICENSE

[LGPLv3](http://www.gnu.org/licenses/lgpl.html)

## SYSTEM REQUIREMENTS

- [Java](http://www.oracle.com/technetwork/java/javase/downloads/) 6+ (Build & Run)
- [Apache Maven](http://maven.apache.org) 3.0.4+ (Build)

## RESOURCES

- [Issues Tracker](https://jira.exoplatform.org/browse/AM/)
- [Continuous Integration Job (Unit tests + deployment in maven repository) ![Build Status](https://ci.exoplatform.org/buildStatus/icon?job=addons-manager-master-ci)](https://ci.exoplatform.org/job/addons-manager-master-ci/)
- [Integration Tests Validation Job ![Build Status](https://ci.exoplatform.org/buildStatus/icon?job=addons-manager-master-ci)](https://ci.exoplatform.org/job/addons-manager-master-ci/)
- [Reporting Job (Sonar + Maven website) ![Build Status](https://ci.exoplatform.org/buildStatus/icon?job=addons-manager-master-reporting)](https://ci.exoplatform.org/job/addons-manager-master-reporting/)
- [Sonar Quality dashboard](https://sonar.exoplatform.org/dashboard/index/org.exoplatform.platform:addons-manager)
- [Maven website](https://projects.exoplatform.org/addons-manager/)
- Maven artifacts
  - [Snapshots](https://repository.exoplatform.org/content/repositories/exo-snapshots/org/exoplatform/platform/addons-manager/)
  - [Releases](https://repository.exoplatform.org/content/repositories/exo-releases/org/exoplatform/platform/addons-manager/)
- [Specifications](http://community.exoplatform.com/portal/intranet/wiki/group/spaces/platform_41/Add-ons_Manager)

## QUICKSTART

```bash
git clone git@github.com:Meeds-io/addons-manager.git && mvn package
```

Unpack the content of the generated archive ```target/addons-manager-VERSION.zip``` into you Platform installation directory
and then use the script ```addon.bat``` on windows systems and ```addon``` on linux/unix systems.

## Usage

We are using ```addon``` in our samples. If you are on a windows system, just use ```addon.bat``` instead.

### Basic Commands

Display all available addons :

```bash
addon list
```

Display all available addons including development versions (snapshots) :

```bash
addon list --snapshots
```

Display all available addons including unstable versions (alpha, beta, ...) :

```bash
addon list --unstable
```

Display all installed addons in your platform server :

```bash
addon list --installed
```

Display all installed addons with an existing more recent stable version :

```bash
addon list --outdated
```

Display all installed addons with an existing more recent stable or snapshot version :

```bash
addon list --outdated  --snapshots
```

Display all installed addons with an existing more recent stable or unstable version :

```bash
addon list --outdated --unstable
```

Install the latest stable version of the add-on ```foo```

```bash
addon install foo
```

Install the latest stable or development version of the add-on ```foo```

```bash
addon install foo --snapshots
```

Install the latest stable or unstable version of the add-on ```foo```

```bash
addon install foo --unstable
```

Install the version ```42.0``` of the add-on ```foo```

```bash
addon install foo:42.0
```

Enforce to reinstall the latest stable version of the add-on ```foo```

```bash
addon install foo --force
```

Uninstall the add-on ```foo```

```bash
addon uninstall foo
```

### Authentication

The add-ons manager supports authentication for accessing protected catalogs or add-on repositories. Authentication can be configured using the ```ADDONSMGR_PROPERTIES``` environment variable.

#### Basic Authentication

To use basic authentication, set the following system properties:

- ```addonsmgr.auth.type=basic```
- ```addonsmgr.auth.username=<your-username>```
- ```addonsmgr.auth.password=<your-password>```

**Linux/Mac (bash):**
```bash
export ADDONSMGR_PROPERTIES="-Daddonsmgr.auth.type=basic -Daddonsmgr.auth.username=myuser -Daddonsmgr.auth.password=mypass"
./addon list
```

With special characters in password:
```bash
export ADDONSMGR_PROPERTIES="-Daddonsmgr.auth.type=basic -Daddonsmgr.auth.username=myuser -Daddonsmgr.auth.password='p@ssw0rd!@#\$%'"
./addon list
```

**Windows (Command Prompt):**
```bash
set ADDONSMGR_PROPERTIES=-Daddonsmgr.auth.type=basic -Daddonsmgr.auth.username=myuser -Daddonsmgr.auth.password=mypass
addon.bat list
```

With spaces in username or password:
```bash
set ADDONSMGR_PROPERTIES=-Daddonsmgr.auth.type=basic -Daddonsmgr.auth.username="my user" -Daddonsmgr.auth.password="my pass"
addon.bat list
```

#### Bearer Token Authentication

To use bearer token authentication, set the following system properties:

- ```addonsmgr.auth.type=bearer```
- ```addonsmgr.auth.token=<your-token>```

**Linux/Mac (bash):**
```bash
export ADDONSMGR_PROPERTIES="-Daddonsmgr.auth.type=bearer -Daddonsmgr.auth.token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
./addon list
```

**Windows (Command Prompt):**
```bash
set ADDONSMGR_PROPERTIES=-Daddonsmgr.auth.type=bearer -Daddonsmgr.auth.token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
addon.bat list
```

#### JSON-based Credentials File

For more complex scenarios, such as multiple servers with different credentials, you can use a JSON-based credentials file. This file supports URL regex matching and environment variable interpolation.

To use a credentials file, set the following system property:

- ```addonsmgr.auth.credentials.file=<path-to-json-file>```

**Example JSON file:**
```json
[
  {
    "url": "https://repository.exoplatform.org/.*",
    "type": "basic",
    "username": "user1",
    "password": "${USER1_PASSWORD}"
  },
  {
    "url": "https://other.server.com/api/.*",
    "type": "bearer",
    "token": "${OTHER_TOKEN}"
  }
]
```

**Linux/Mac (bash):**
```bash
export ADDONSMGR_PROPERTIES="-Daddonsmgr.auth.credentials.file=/path/to/credentials.json"
./addon list
```

**Windows (Command Prompt):**
```bash
set ADDONSMGR_PROPERTIES=-Daddonsmgr.auth.credentials.file=C:\\path\\to\\credentials.json
addon.bat list
```

**Notes on JSON format:**
- The file can be a JSON array or a JSON object with a `credentials` property containing the array.
- `url`: A string or a regex pattern to match the download URL.
- `type`: `basic` or `bearer`.
- `${VAR_NAME}`: Will be replaced by the value of the environment variable `VAR_NAME`.

#### Multiple Properties

You can combine multiple system properties in the ```ADDONSMGR_PROPERTIES``` variable:

**Linux/Mac:**
```bash
export ADDONSMGR_PROPERTIES="-Daddonsmgr.auth.type=basic -Daddonsmgr.auth.username=user -Daddonsmgr.auth.password=pass -Dcustom.property=value"
./addon list
```

**Windows:**
```bash
set ADDONSMGR_PROPERTIES=-Daddonsmgr.auth.type=basic -Daddonsmgr.auth.username=user -Daddonsmgr.auth.password=pass -Dcustom.property=value
addon.bat list
```

#### Notes

- Authentication is optional and only applied when the ```ADDONSMGR_PROPERTIES``` environment variable is set
- The authentication type is case-insensitive (both "basic" and "BASIC" work)
- For basic authentication, both username and password must be provided
- For bearer authentication, the token must be provided
- If authentication is configured but credentials are missing, the download will fail with an appropriate error message

## BUILD (AND AUTOMATED TESTS)

To build the project you launch

```bash
mvn verify
```

You can additionally activate the execution of integration tests with

```bash
mvn verify -Prun-its
```

To deactivate all automated tests

```bash
mvn verify -DskipTests
```