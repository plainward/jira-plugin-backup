# Plugin Backup for Jira

Download and backup user-installed Jira plugins as JAR files. View all installed plugins in a single admin page, download individually, or export everything as a ZIP archive.

## Why

Jira administrators often need to backup installed plugins before upgrades, migrations, or disaster recovery. This plugin provides a simple way to download all user-installed plugin JARs — either one by one or as a single ZIP file.

**Use cases:**
- Backup plugins before a Jira version upgrade
- Export plugins when migrating to a new instance
- Keep an offline copy of your plugin set for disaster recovery
- Audit which plugins are installed and their file sizes

## Compatibility

| Platform | Version |
|----------|---------|
| Jira Data Center | 9.x and above |
| Java | 8+ |

Tested on Jira Data Center 9.12.30.

## Installation

Upload the JAR file through your Jira admin panel:

**Administration → Manage apps → Upload app**

Or install directly from the [Atlassian Marketplace](https://marketplace.atlassian.com/).

## Usage

### Admin UI

1. Go to **Administration** (gear icon) → left sidebar → **Plugin Backup**
2. You will see a table listing all user-installed plugins with their names, sizes, and modification dates
3. Click **Download** next to any plugin to save its JAR file
4. Click **Download All as ZIP** to export all plugins in a single archive

**URL:** `https://your-jira.com/plugins/servlet/plugin-backup`

### REST API

All endpoints require Jira administrator permissions. Authentication via session cookie or Basic Auth.

#### List installed plugins

```
GET /rest/plugin-backup/1.0/list
```

**Example:**

```bash
curl -u admin:password https://your-jira.com/rest/plugin-backup/1.0/list
```

**Response:**

```json
[
  {
    "name": "epic-cloner-1.0.0.jar",
    "size": 319488,
    "modified": "2026-04-05 20:20"
  },
  {
    "name": "markdown-extra-2.1.0.jar",
    "size": 1548200,
    "modified": "2026-03-15 14:30"
  },
  {
    "name": "assignee-changer-1.0.26.jar",
    "size": 245760,
    "modified": "2026-02-10 09:15"
  }
]
```

#### Download a single plugin JAR

```
GET /rest/plugin-backup/1.0/download?name={filename}
```

**Example:**

```bash
curl -u admin:password -OJ \
  https://your-jira.com/rest/plugin-backup/1.0/download?name=epic-cloner-1.0.0.jar
```

Returns the JAR file with `Content-Disposition: attachment` header.

#### Download all plugins as ZIP

```
GET /rest/plugin-backup/1.0/download-all
```

**Example:**

```bash
curl -u admin:password -OJ \
  https://your-jira.com/rest/plugin-backup/1.0/download-all
```

Returns a ZIP archive named `jira-plugins-YYYY-MM-DD.zip` containing all user-installed plugin JARs.

**Example with PAT (Personal Access Token):**

```bash
curl -H "Authorization: Bearer YOUR_PAT_TOKEN" -OJ \
  https://your-jira.com/rest/plugin-backup/1.0/download-all
```

## Access Control

All endpoints and the admin page are restricted to members of the `jira-administrators` group. Non-admin users receive a `403 Forbidden` response.

## Privacy

This plugin runs entirely within your Jira instance. It does not transmit any data to external servers, does not collect analytics or telemetry, and does not require an internet connection.

## License

Free. No license key required.

## Vendor

**Plainward Software**
https://plainward.com

Support: support@plainward.com
