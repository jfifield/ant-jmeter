# JMeter Ant Task

This is an [Ant](http://ant.apache.org/) task for automating running [JMeter](http://jmeter.apache.org) test plans. The task executes one or more JMeter test plans, and logs the results to a file.

To use the task, you must have JMeter installed. You must also include [ant-jmeter-1.1.1.jar](https://github.com/jfifield/ant-jmeter/releases/download/1.1.1/ant-jmeter-1.1.1.jar) in your Ant classpath. Adding the jar to $ANT_HOME/lib will make this happen automatically.

Start by defining the task to make it available to your build script:

```xml
<taskdef
    name="jmeter"
    classname="org.programmerplanet.ant.taskdefs.jmeter.JMeterTask"/>
```

Set the jmeterhome parameter to your JMeter install location, and the resultlog parameter to the name of a file to log the test results to.

You can either specify a single test plan using the testplan parameter, or multiple test plans using the testplans nested element. The testplans element is a standard Ant [FileSet](http://ant.apache.org/manual/Types/fileset.html) element.

```xml
<jmeter
    jmeterhome="c:\jakarta-jmeter-1.8.1"
    testplan="${basedir}/loadtests/JMeterLoadTest.jmx"
    resultlog="${basedir}/loadtests/JMeterResults.jtl"/>
```

```xml
<jmeter
    jmeterhome="c:\jakarta-jmeter-1.8.1"
    resultlog="${basedir}/loadtests/JMeterResults.jtl">
    <testplans dir="${basedir}/loadtests" includes="*.jmx"/>
</jmeter>
```

Optional JMeter arguments supported include specifying an alternate jmeter properties file (jmeterproperties), running remote servers specified in jmeter properties file (runremote), and running the tests through a proxy or firewall (proxyhost, proxyport, proxyuser, proxypass).

Setting the failureProperty attribute will set the specified property to "true" in the event of a JMeter test failure. This gives you the opportunity to take further action such as send an email or fail the ant build.

You can override JMeter properties (instead of modifying jmeter.properties) like this:

```xml
<jmeter
    jmeterhome="c:\jakarta-jmeter-1.8.1"
    testplan="${basedir}/loadtests/JMeterLoadTest.jmx"
    resultlog="${basedir}/loadtests/JMeterResults.jtl">
    <property name="request.threads" value="1"/>
    <property name="request.loop" value="10"/>
</jmeter>
```

You may also specify additional JVM arguments to the JVM launched to run JMeter. Here is an example of how to specify JVM arguments:

```xml
<jmeter
    jmeterhome="c:\jakarta-jmeter-1.8.1"
    testplan="${basedir}/loadtests/JMeterLoadTest.jmx"
    resultlog="${basedir}/loadtests/JMeterResults.jtl">
    <jvmarg value="-Xincgc"/>
    <jvmarg value="-Xmx128m"/>
    <jvmarg value="-Dproperty=value"/>
</jmeter>
```

I've also included an XSLT file, [jmeter-results-report.xsl](xslt/jmeter-results-report.xsl), for generating a summary report from the result log file. The summary report is very similar to the default report created by the junitreport task. You can use the xslt task to create the report:

```xml
<xslt
    in="${basedir}/loadtests/JMeterResults.jtl"
    out="${basedir}/loadtests/JMeterResults.html"
    style="${basedir}/loadtests/jmeter-results-report.xsl"/>
```

Note: If you are using JMeter 2.1 or later, you must use the new xslt stylesheet(s) included in the JMeter extras directory. The new stylesheets have been modified to support the new JMeter log file format.

If you would like failure detail messages in the report output, you must configure JMeter to output that information to the result log. To do this, set the following property in your jmeter.properties file before running the test plans:

```
jmeter.save.saveservice.assertion_results=all
```

Note: As of JMeter 1.9RC2(?), the default results output format is now csv. It must be changed to xml in order to use the xslt task to create the html report:

```
jmeter.save.saveservice.output_format=xml
```

The report will look something like this:

![](img/JMeterResults.jpg?raw=true)

There is also another XSLT file that was contributed which generates an enhanced report that includes expandable details. Use [jmeter-results-detail-report.xsl](xslt/jmeter-results-detail-report.xsl) with the images [expand.jpg](xslt/expand.jpg) and [collapse.jpg](xslt/collapse.jpg). Note: I have not tested it on all browsers.

![](img/JMeterResultsDetail.jpg?raw=true)

## Parameters

| ATTRIBUTE | DESCRIPTION | REQUIRED |
| --- | --- | --- |
| jmeterhome | JMeter install location. | Yes |
| testplan | The location of the test plan file. | Either testplan or testplans |
| resultlog | The location of the result log file. | Either resultlog or resultlogdir |
| resultlogdir | The directory to place result log files. When used, result log file names will match the test plan files names, with the extension renamed from .jmx to .jtl. | Either resultlog or resultlogdir |
| failureproperty | The name of a property to set to "true" in the event of a test plan failure. | No |
| jmeterproperties | The location of an alternate jmeter.properties file to use. | No |
| jmeterlogfile | The location of the JMeter log file. | No |
| runremote | If "true", runs remote servers specified in jmeter.properties. Default is "false". | No |
| proxyhost | Host name of a proxy to run the tests through. | No |
| proxyport | Port of the proxy host specified. | No |
| proxyuser | Username of the proxy host specified. | No |
| proxypass | Password of the proxy host specified. | No |

## Nested Elements

| ELEMENT | DESCRIPTION |
| --- | --- |
| testplans | Use instead of testplan attribute when you want to specify multiple test plan files. This element is a standard Ant [FileSet](http://ant.apache.org/manual/Types/fileset.html) element. |
| property | Use to specify additional JMeter properties (instead of modifying jmeter.properties file). Attributes include name, value and remote. When the remote attribute is true, the property will be sent to all remote servers (default is false). |
| jvmarg | Use to specify JVM arguments to the JVM launched to run JMeter. The only attribute is value. |
| jmeterarg | Use to specify additional JMeter command line arguments. The only attribute is value. |

## License

[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0)

## FAQ

Q1. Why do I receive the message "Error! Content is not allowed in prolog." when running the xslt task to generate the html report?

A1. Most likely the JMeter log output format is CSV instead of XML. Ensure the JMeter property jmeter.save.saveservice.output_format is set to xml.

Q2. Why is my generated report always empty with 'NaN' values in the summary?

A2. You might be using the pre-JMeter 2.1 xslt stylesheet(s) with JMeter 2.1 or later. If you are using JMeter 2.1 or later, ensure you are using the new xslt stylesheet(s) included in the JMeter extras directory.

## Changes

12/5/2011 – added jmeterlogfile attribute to support specifying the path to a JMeter log file

9/2/2011 – added remote attribute to <property> element to support sending properties to remote JMeter servers

4/12/2008 – added support for arbritrary JMeter arguments via nested <jmeterarg> element (contributed)

5/12/2007 – added resultlogdir attribute to support creating one result log file per test plan

1/28/2007 – added support for failure detection for JMeter 2.1

5/31/2006 – removed forceBuildFailure support in favor of new failureProperty – if a test plan fails, the specified property will be set to 'true', allowing more flexibility in the ant script (contributed)

7/30/2005 – added support for ant test failure via forceBuildFailure attribute (contributed)

5/6/2005 – updated jmeter-results-detail-report.xsl: links from Pages section entries to corresponding Failure Detail entries, optional display of response Data in Failure Detail entries (contributed)

1/21/2004 – added support for additional jmeter arguments (proxyuser, proxypass), and new support for specifying jmeter properties (like the jmeter -J argument) using the <property> tag

10/4/2003 – added support for additional jmeter arguments (jmeterproperties, runremote, proxyhost, proxyport), and included updated jmeter-results-detail-report.xsl with contributed fixes to work in more browsers.

7/29/2003 – added support for jvm arguments (jvmarg).

3/3/2003 – original version
