newrelic-unix-plugin
====================

# New Relic Plugin for Unix (AIX, Linux, Solaris/SunOS) systems

----

## What's new in V2?

This plugin has been upgraded to V2 of the New Relic Platform Java SDK, which helps to simplify and the installation experience, and adds a couple of key features:

* 'newrelic.properties' file is now 'newrelic.json'
* Plugin configuration is now done through 'plugin.json'
* Logging configuration has been simplified and consolidated to 'newrelic.json'
* HTTP/S proxies are now supported using 'newrelic.json'

----

## Requirements

- A New Relic account. Sign up for a free account [here](http://newrelic.com)
- A unix server that you want to monitor
- Java Runtime (JRE) environment Version 1.6 or later
- Network access to New Relic (proxies are supported, see details below)

----

## Installation & Usage Overview

1. Download the latest version of the agent: https://github.com/sschwartzman/newrelic-unix-plugin/archive/master.zip
2. Unzip on Unix server that you want to monitor
3. Configure `config/newrelic.json`
4. Copy `config/plugin.json` from the OS-specific templates in `config` and configure that file
5. Configure `pluginctl.sh` to have the correct paths to Java and your plugin location
  * Set `PLUGIN_JAVA_HOME` to location of Java on your server (up to but excluding the /bin directory)
  * Set `PLUGIN_PATH` to fully qualified location of the Unix Plugin
6. Run `./pluginctl.sh start` from command-line
7. Check `logs/newrelic_unix_plugin.log` for errors
8. Login to New Relic UI and find your plugin instance
  * In the New Relic UI, select "Plugins" from the top level accordion menu 
  * Check for the "Unix" plugin in left-hand column.  Click on it, your instance should appear in the list.

----

### Configuring the `newrelic.json` file

The `newrelic.json` is a standardized file containing configuration information that applies to any plugin (e.g. license key, logging, proxy settings), so going forward you will be able to copy a single `newrelic.json` file from one plugin to another.  Below is a list of the configuration fields that can be managed through this file:

#### Configuring your New Relic License Key

* Your New Relic license key is the only required field in the `newrelic.json` file as it is used to determine what account you are reporting to.  If you do not know what your license key is, you can learn about it [here](https://newrelic.com/docs/subscriptions/license-key).
* Your license key can be found in New Relic UI, on 'Account settings' page.

##### Example: 

```
{
  "license_key": "YOUR_LICENSE_KEY_HERE"
}
```

#### Logging configuration

* By default Platform plugins will have their logging turned on; however, you can manage these settings with the following configurations:

* `log_level` - The log level. Valid values: [`debug`, `info`, `warn`, `error`, `fatal`]. Defaults to `info`.
* `log_file_name` - The log file name. Defaults to `newrelic_plugin.log`.
* `log_file_path` - The log file path. Defaults to `logs`.
* `log_limit_in_kbytes` - The log file limit in kilobytes. Defaults to `25600` (25 MB). If limit is set to `0`, the log file size would not be limited.

##### Example

```
{
  "license_key": "YOUR_LICENSE_KEY_HERE"
  "log_level": "info",
  "log_file_path": "/var/log/newrelic",
  "log_limit_in_kbytes": "4096"
  
}
```

#### Proxy configuration

If you are running your plugin from a machine that runs outbound traffic through a proxy, you can use the following optional configurations in your `newrelic.json` file:

`proxy_host` - The proxy host (e.g. `webcache.example.com`)
`proxy_port` - The proxy port (e.g. `8080`).  Defaults to `80` if a `proxy_host` is set
`proxy_username` - The proxy username
`proxy_password` - The proxy password

##### Examples

```
{
  "license_key": "YOUR_LICENSE_KEY_HERE",
  "proxy_host": "proxy.mycompany.com",
  "proxy_port": 9000
}
```

```
{
  "license_key": "YOUR_LICENSE_KEY_HERE",
  "proxy_host": "proxy.mycompany.com",
  "proxy_port": "9000",
  "proxy_username": "my_user",
  "proxy_password": "my_password"
}
```

### Configuring the `plugin.json` file

The `plugin.json` file contains the list of OS level commands that you want to execute as part of the plugin. All current possibilities for each OS are found in the `config/plugin.json.[OS]` template files.
To properly set up the agent for your OS, copy one of these template to `plugin.json`. 

Each command will get its own object in the `agents` array, as seen in the Example below.
`command` is the only required configuration for each object. Commands in lowercase are ones literally defined in the plugin (i.e. 'iostat'), whereas commands in Caps are specialized variations on those commands (i.e. `VirtualMemory`). 

##### Optional Configurations for `plugin.json`

For each command, the following optional configurations are available:

`OS` - The OS you are monitoring. If left out, it will use the "auto" setting, in which the plugin will detect your OS type. 
 * Normally the "auto" setting works fine. If not, you can define it as any of: [aix, linux, sunos].
`debug` - This is an extra debug setting to use when a specific command isn't reporting properly. Enabling it will do 2 things:
  1. It will expose the parsing details of that command
  2. It will not send metrics to New Relic
  * Note: Normally, you will use the `debug` setting in newrelic.json to log extra information, such as agent connectivity info. This debug setting is specific to command parsing issues.
  
##### Examples 

Normally, this is what your plugin.json should look like (this example is pulled from the linux template):

```
{
  "agents": [
  	{
    	"command": "df"
    },
    {
		"command": "free"
    },
    {
		"command": "iostat"
    },
    {
		"command": "vmstat"
    },
    {
		"command": "IostatMb"
    },
    {
		"command": "VmstatTotals"
    }
  ]
}
```

Here is an example with the optional configurations:

```
{
  "agents": [
  	{
    	"OS": "linux",
    	"command": "df",
    	"debug": true
    },
    {
		"OS": "linux",
		"command": "free",
		"debug": false
    },
    {
		"OS": "linux",
		"command": "iostat"
		"debug": true
    }
  ]
}
```




