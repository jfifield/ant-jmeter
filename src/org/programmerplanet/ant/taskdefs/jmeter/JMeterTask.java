package org.programmerplanet.ant.taskdefs.jmeter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Execute;
import org.apache.tools.ant.taskdefs.LogStreamHandler;
import org.apache.tools.ant.types.CommandlineJava;
import org.apache.tools.ant.types.FileSet;

/**
 * Runs one or more JMeter test plans sequentially.
 *
 * @author <a href="mailto:jfifield@programmerplanet.org">Joseph Fifield</a>
 */
public class JMeterTask extends Task {

	/**
	 * The JMeter installation directory.
	 */
	private File jmeterHome;

	/**
	 * The property file to use.
	 */
	private File jmeterProperties;

	/**
	 * The test plan to execute.
	 */
	private File testPlan;

	/**
	 * The file to log results to.
	 */
	private File resultLog;

	/**
	 * A collection of FileSets specifying test plans to execute.
	 */
	private ArrayList testPlans = new ArrayList();

	/**
	 * Whether or not to run the remote servers as specified in the properties file.
	 * Default: false.
	 */
	private boolean runRemote = false;

	/**
	 * The proxy server hostname or ip address.
	 */
	private String proxyHost;

	/**
	 * The proxy server port.
	 */
	private String proxyPort;

	/**
	 * The username for the proxy server.
	 */
	private String proxyUser;

	/**
	 * The password for the proxy server.
	 */
	private String proxyPass;

	/**
	 * The main JMeter jar.
	 */
	private File jmeterJar;

	/**
	 * Array of arguments to be passed to the JVM that will run JMeter.
	 */
	private ArrayList jvmArgs = new ArrayList();

	/**
	 * Array of properties dynamically passed to JMeter
	 */
	private ArrayList jmProperties = new ArrayList();

	/**
	 * Indicate if build to be forcefully failed upon testcase failure.
	 */
	private String failureProperty;

	/**
	 * @see org.apache.tools.ant.Task#execute()
	 */
	public void execute() throws BuildException {
		jmeterJar = new File(jmeterHome.getAbsolutePath() + File.separator + "bin" + File.separator + "ApacheJMeter.jar");

		validate();

		log("Using JMeter Home: " + jmeterHome.getAbsolutePath(), Project.MSG_VERBOSE);
		log("Using JMeter Jar: " + jmeterJar.getAbsolutePath(), Project.MSG_VERBOSE);

		// execute the single test plan if specified
		if (testPlan != null) {
			executeTestPlan(testPlan);
		}

		// execute each of the test plans specified in each of the "testplans" FileSets
		Iterator testPlanIter = testPlans.iterator();
		while (testPlanIter.hasNext()) {
			FileSet fileSet = (FileSet)testPlanIter.next();
			DirectoryScanner scanner = fileSet.getDirectoryScanner(getProject());
			File baseDir = scanner.getBasedir();
			String[] files = scanner.getIncludedFiles();

			for (int i = 0; i < files.length; i++) {
				String testPlanFile = baseDir + File.separator + files[i];
				executeTestPlan(new File(testPlanFile));
			}
		}

		checkForFailures();
	}

	/**
	 * Validate the results.
	 */
	private void checkForFailures() throws BuildException {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(getResultLog()));
			//look for any success="false"
			String line = null;
			while ((line = reader.readLine()) != null) {
				line = line.toLowerCase();
				//throw Error if there are failures.
				if (line.indexOf("success=\"false\"") > 0 || line.indexOf(" s=\"false\"") > 0) {
					setFailure(getFailureProperty());
				}
			}
		} catch (IOException e) {
			setFailure(getFailureProperty());
		} finally {
			try { reader.close(); } catch (Exception e) { /* ignore */ }
		}
	}

	/**
	 * Validate the task attributes.
	 */
	private void validate() throws BuildException {
		if (jmeterHome == null || !jmeterHome.isDirectory()) {
			throw new BuildException("You must set jmeterhome to your JMeter install directory.", getLocation());
		}

		if (!(jmeterJar.exists() && jmeterJar.isFile())) {
			throw new BuildException("jmeterhome does not appear to contain a valid JMeter installation.", getLocation());
		}

		if (resultLog == null) {
			throw new BuildException("You must set resultLog.", getLocation());
		}
	}

	/**
	 * Execute a JMeter test plan.
	 */
	private void executeTestPlan(File testPlanFile) {
		log("Executing test plan: " + testPlanFile, Project.MSG_INFO);

		CommandlineJava cmd = new CommandlineJava();

		cmd.setJar(jmeterJar.getAbsolutePath());

		// Set the JVM args
		Iterator jvmArgIterator = jvmArgs.iterator();
		while (jvmArgIterator.hasNext()) {
			Arg jvmArg = (Arg)jvmArgIterator.next();
			cmd.createVmArgument().setValue(jvmArg.getValue());
		}

		// non-gui mode
		cmd.createArgument().setValue("-n");
		// the properties file
		if (jmeterProperties != null) {
			cmd.createArgument().setValue("-p");
			cmd.createArgument().setValue(jmeterProperties.getAbsolutePath());
		}
		// the test plan file
		cmd.createArgument().setValue("-t");
		cmd.createArgument().setValue(testPlanFile.getAbsolutePath());
		// the result log file
		cmd.createArgument().setValue("-l");
		cmd.createArgument().setValue(resultLog.getAbsolutePath());
		//run remote servers?
		if (runRemote) {
			cmd.createArgument().setValue("-r");
		}

		// the proxy host
		if ((proxyHost != null) && (proxyHost.length() > 0)) {
			cmd.createArgument().setValue("-H");
			cmd.createArgument().setValue(proxyHost);
		}
		// the proxy port
		if ((proxyPort != null) && (proxyPort.length() > 0)) {
			cmd.createArgument().setValue("-P");
			cmd.createArgument().setValue(proxyPort);
		}
		// the proxy user
		if ((proxyUser != null) && (proxyUser.length() > 0)) {
			cmd.createArgument().setValue("-u");
			cmd.createArgument().setValue(proxyUser);
		}
		// the proxy password
		if ((proxyPass != null) && (proxyPass.length() > 0)) {
			cmd.createArgument().setValue("-a");
			cmd.createArgument().setValue(proxyPass);
		}

		// the JMeter runtime properties
		Iterator jmPropertyIterator = jmProperties.iterator();
		while (jmPropertyIterator.hasNext()) {
			Property jmProperty = (Property)jmPropertyIterator.next();
			if (jmProperty.isValid()) {
				cmd.createArgument().setValue("-J" + jmProperty.toString());
			}
		}

		Execute execute = new Execute(new LogStreamHandler(this, Project.MSG_INFO, Project.MSG_WARN));
		execute.setCommandline(cmd.getCommandline());
		execute.setAntRun(getProject());

		execute.setWorkingDirectory(new File(jmeterHome.getAbsolutePath() + File.separator + "bin"));
		log(cmd.describeCommand(), Project.MSG_VERBOSE);

		try {
			execute.execute();
		} catch (IOException e) {
			throw new BuildException("JMeter execution failed.", e, getLocation());
		}
	}

	public void setJmeterHome(File jmeterHome) {
		this.jmeterHome = jmeterHome;
	}

	public File getJmeterHome() {
		return jmeterHome;
	}

	public void setJmeterProperties(File jmeterProperties) {
		this.jmeterProperties = jmeterProperties;
	}

	public File getJmeterProperties() {
		return jmeterProperties;
	}

	public void setTestPlan(File testPlan) {
		this.testPlan = testPlan;
	}

	public File getTestPlan() {
		return testPlan;
	}

	public void setResultLog(File resultLog) {
		this.resultLog = resultLog;
	}

	public File getResultLog() {
		return resultLog;
	}

	public void addTestPlans(FileSet set) {
		testPlans.add(set);
	}

	public void addJvmarg(Arg arg) {
		jvmArgs.add(arg);
	}

	public void setRunRemote(boolean runRemote) {
		this.runRemote = runRemote;
	}

	public boolean getRunRemote() {
		return runRemote;
	}

	public void setProxyHost(String proxyHost) {
		this.proxyHost = proxyHost;
	}

	public String getProxyHost() {
		return proxyHost;
	}

	public void setProxyPort(String proxyPort) {
		this.proxyPort = proxyPort;
	}

	public String getProxyPort() {
		return proxyPort;
	}

	public void setProxyUser(String proxyUser) {
		this.proxyUser = proxyUser;
	}

	public String getProxyUser() {
		return proxyUser;
	}

	public void setProxyPass(String proxyPass) {
		this.proxyPass = proxyPass;
	}

	public String getProxyPass() {
		return proxyPass;
	}

	public void addProperty(Property property) {
		jmProperties.add(property);
	}

	public void setFailureProperty(String failureProperty) {
		this.failureProperty = failureProperty;
	}

	public String getFailureProperty() {
		return failureProperty;
	}

	public void setFailure(String failureProperty) {
		getProject().setProperty(failureProperty, "true");
	}

}
