Table of Contents
=================


<!-- vim-markdown-toc GFM -->

* [Problem 1: How to Change Open Files and Maximum Processes limits on MacOS](#problem-1-how-to-change-open-files-and-maximum-processes-limits-on-macos)
    * [What does NOT work](#what-does-not-work)
    * [What DOES work](#what-does-work)
        * [To check the current limits on your Mac OS X system, run:](#to-check-the-current-limits-on-your-mac-os-x-system-run)
        * [Adjusting Open File Limits](#adjusting-open-file-limits)
* [Problem 2: Changing the Ephemeral Port Limit](#problem-2-changing-the-ephemeral-port-limit)

<!-- vim-markdown-toc -->

# Problem 1: How to Change Open Files and Maximum Processes limits on MacOS
By default the number of file descriptors that a process is allowed to open and the number of processes a user is allowed to open is restricted.
These limits can be seen by using the commands `ulimit -Ha`  (for Hard Limits) and `ulimit -Sa` for Soft Limits.

[This](https://unix.stackexchange.com/questions/108174/how-to-persistently-control-maximum-system-resource-consumption-on-mac) is a good writeup that explains the meaning of the 'Hard' and 'Soft' limits.

The aim of this guide is to detail the macOS tuning needed in order to run a Gatling test with a useful load profile.
These instructions are only meant to enable the execution of gatling based load test simulations to be run on a dev machine.
Without these settings when running Gatling tests, you might see issues such as 'Caused by: java.net.SocketException: Too many open files' which will be interpreted falsely by Gatling as failed tests even though there may not be a problem with the application itself.

## What does NOT work
When you attempt to fine tune macOS for Gatling, you may be led down a number of paths which don't have any effect on the relevant system parameters.
In order to prevent you from wasting time on these red herrings, here is a list of things that I found ++**did not work**++:

1. Using ﻿`-XX:-MaxFDLimit` JVM Arg either via
* The `.sbtopts` file in the root of the project with the following contents:
```shell
-J-Xmx3536M
-J-Xss1M
-J-XX:+CMSClassUnloadingEnabled
-J-XX:+UseConcMarkSweepGC
-J-XX:MaxPermSize=724M
-J-XX:-MaxFDLimit
```

* Attempting to pass in `-XX:-MaxFDLimit` via the sbt build:

```scala
javaOptions in Gatling := overrideDefaultJavaOptions("-XX:-MaxFDLimit")
```

* Attempting to use `-XX:-MaxFDLimit` as a default JVM option in the hope that it gets picked up.

The problem with all of the above approaches is not that `-XX:-MaxFDLimit` is not picked up, but that it has no effect whatsoever.


2. In a non-root session, attempting to set the ulimit values for maximum number of allowed processes (`ulimit -h`) or  maximum number of open file descriptors (`ulimit -n`) does not work. These values *can* be changed by the root user, but then the gatling test would have to be run as the root user as well.


## What DOES work

### To check the current limits on your Mac OS X system, run:

```shell
launchctl limit maxfiles
```

The last two columns are the soft and hard limits, respectively.

### Adjusting Open File Limits

To adjust open files limits on a system-wide basis, you must create **two** configuration files. The first is a property list (aka plist) file in `/Library/LaunchDaemons/limit.maxfiles.plist` that contains the following XML configuration:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
  <plist version="1.0">
    <dict>
      <key>Label</key>
        <string>limit.maxfiles</string>
      <key>ProgramArguments</key>
        <array>
          <string>launchctl</string>
          <string>limit</string>
          <string>maxfiles</string>
          <string>1048576</string>
          <string>1048600</string>
        </array>
      <key>RunAtLoad</key>
        <true/>
      <key>ServiceIPC</key>
        <false/>
    </dict>
  </plist>
```

This will set the open files limit to 200000. The second plist configuration file should be stored in `/Library/LaunchDaemons/limit.maxproc.plist` with the following contents:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple/DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
  <plist version="1.0">
    <dict>
      <key>Label</key>
        <string>limit.maxproc</string>
      <key>ProgramArguments</key>
        <array>
          <string>launchctl</string>
          <string>limit</string>
          <string>maxproc</string>
          <string>2048</string>
          <string>2048</string>
        </array>
      <key>RunAtLoad</key>
        <true />
      <key>ServiceIPC</key>
        <false />
    </dict>
  </plist>
```

Both plist files must be owned by root:wheel and have permissions -rw-r--r--. This permissions should be in place by default, but you can ensure that they are in place by running sudo chmod 644 <filename>. While the steps explained above will cause system-wide open file limits to be correctly set upon restart, you can apply them manually by running launchctl limit.


The above plists are attached to this project. You can execute the following commands to do the needful:

```shell
sudo mv limit.maxfiles.plist /Library/LaunchDaemons
sudo mv limit.maxproc.plist /Library/LaunchDaemons

sudo chown root:wheel /Library/LaunchDaemons/limit.maxfiles.plist
sudo chown root:wheel /Library/LaunchDaemons/limit.maxproc.plist

sudo launchctl load -w /Library/LaunchDaemons/limit.maxfiles.plist
sudo launchctl load -w /Library/LaunchDaemons/limit.maxproc.plist
```

Finally, **remember to reboot your machine!**

But, we are not done yet !

# Problem 2: Changing the Ephemeral Port Limit

The next problem encountered is due to the limited number of ports available to create connections on as well as the time that is required to elapse before a once-used port becomes available to be used again.

This limit is visible by using the following:

```shell
$ sysctl net.inet.ip.portrange.first net.inet.ip.portrange.last
net.inet.ip.portrange.first: 49152
net.inet.ip.portrange.last: 65535
```

Additionally, once a port has been used, [TIME_WAIT](http://www.softlab.ntua.gr/facilities/documentation/unix/unix-socket-faq/unix-socket-faq-2.html#ss2.7)
must elapse before it can be reused again. This duration is 15 secons on macOS

```shell
$ sysctl net.inet.tcp.msl
net.inet.tcp.msl: 15000
```

To change the avaiable number of ports and to allow a port to become reusable quicker, the following can be done:

1.  Increase the available port range:
```shell
$ sudo sysctl -w net.inet.ip.portrange.first=32768
  Password:
  net.inet.ip.portrange.first: 49152 -> 32768
```

2. Reduce the TIME_WAIT:
```shell
   $ sudo sysctl -w net.inet.tcp.msl=1000
   net.inet.tcp.msl: 15000 -> 1000
```

On one machine, after making all of the above settings, the failure rate went down from around 50% to 0%:

```shell
---- Response Time Distribution ------------------------------------------------
> t < 800 ms                                         48138 ( 48%)
> 800 ms < t < 1200 ms                                   1 (  0%)
> t > 1200 ms                                            0 (  0%)
> failed                                             51861 ( 52%)
---- Errors --------------------------------------------------------------------
> j.n.ConnectException: Can't assign requested address: localhos  51794 (99.87%)
t/127.0.0.1:9000
> status.find.in(200,304,201,202,203,204,205,206,207,208,209), b     67 ( 0.13%)
ut actually found 500
================================================================================
```

```shell
---- Response Time Distribution ------------------------------------------------
> t < 800 ms                                        100000 (100%)
> 800 ms < t < 1200 ms                                   0 (  0%)
> t > 1200 ms                                            0 (  0%)
> failed                                                 0 (  0%)
================================================================================
```

Note that the above sysctl changes only last for the shell they are executed in and will not be propagated to a new shell or after an OS restart.
They can be preserved by adding them to /etc/sysctl.conf :

```
$ cat /etc/sysctl.conf
net.inet.ip.portrange.first=32768
net.inet.tcp.msl=1000
```
