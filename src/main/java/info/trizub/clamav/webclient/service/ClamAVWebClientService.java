package info.trizub.clamav.webclient.service;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.UnresolvedAddressException;
import java.nio.file.Paths;
import java.util.Properties;

import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;

import xyz.capybara.clamav.ClamavClient;
import xyz.capybara.clamav.ClamavException;
import xyz.capybara.clamav.Platform;
import xyz.capybara.clamav.commands.scan.result.ScanResult;

@Service
public class ClamAVWebClientService {

	private final static String CLAMAV_SERVICE_PROPERTIES_FILE = "conf/clamav-service.properties";
	private final static String CLAMAV_SERVICE_HOST_PROPERTY = "clamav.service.host";
	private final static String CLAMAV_SERVICE_PORT_PROPERTY = "clamav.service.port";
	private final static String CLAMAV_SERVICE_PLATFORM_PROPERTY = "clamav.service.platform";
	private final static String MODEL_STATUS_ATTRIBUTE = "status";
	private final static String MODEL_RESPONSE_ATTRIBUTE = "response";
	private final static String MODEL_SERVER_HOST_ATTRIBUTE = "host";
	private final static String MODEL_SERVER_PORT_ATTRIBUTE = "port";
	private final static String MODEL_SERVER_PLATFORM_ATTRIBUTE = "platform";

	private ClamavClient client;
	private String status;
	private String response;
	private String serverHost;
	private String serverPort;
	private String serverPlatform;

	public ClamAVWebClientService() {
		initState();
		initServerSettings();
	}

	private void initState() {
		status = null;
		response = null;
	}

	private void setState(Model model) {
		model.addAttribute(MODEL_STATUS_ATTRIBUTE, status);
		model.addAttribute(MODEL_RESPONSE_ATTRIBUTE, response);
	}

	private void initServerSettings() {
		client = null;
		serverHost = null;
		serverPort = null;
		serverPlatform = null;
	}

	private ClamavClient getClamavClient() throws IOException {
		if (client == null) {
			loadServerProperties();
			Platform platform = null;
			if (serverPlatform != null) {
				if (serverPlatform.equalsIgnoreCase(Platform.UNIX.name())) {
					platform = Platform.UNIX;
				} else if (serverPlatform.equalsIgnoreCase(Platform.WINDOWS.name())) {
					platform = Platform.WINDOWS;
				} else if (serverPlatform.equalsIgnoreCase(Platform.WINDOWS.name())) {
					platform = Platform.JVM_PLATFORM;
				}
			}
			if (platform != null) {
				client = new ClamavClient(serverHost, Integer.parseInt(serverPort), platform);
			} else {
				client = new ClamavClient(serverHost, Integer.parseInt(serverPort));
			}
		}
		return client;
	}

	private void loadServerProperties() throws IOException {
		initServerSettings();
		Properties props = new Properties();
		try (FileInputStream in = new FileInputStream(CLAMAV_SERVICE_PROPERTIES_FILE)) {
			props.load(in);
			if (props != null && !props.isEmpty()) {
				if (props.containsKey(CLAMAV_SERVICE_HOST_PROPERTY)) {
					serverHost = props.getProperty(CLAMAV_SERVICE_HOST_PROPERTY);
				}
				if (props.containsKey(CLAMAV_SERVICE_PORT_PROPERTY)) {
					serverPort = props.getProperty(CLAMAV_SERVICE_PORT_PROPERTY);
				}
				if (props.containsKey(CLAMAV_SERVICE_PLATFORM_PROPERTY)) {
					serverPlatform = props.getProperty(CLAMAV_SERVICE_PLATFORM_PROPERTY);
				}
			}
		}
	}

	public void setServerProperties(Model model) {
		initState();
		try {
			loadServerProperties();
			status = "success";
			response = "ClamAV service properties have been loaded...";
		} catch (ClamavException | IOException ex) {
			status = "error";
			if (ex.getCause() instanceof UnresolvedAddressException) {
				response = "ClamAV service unavailable. Please check connection properties.";
			} else if (ex.getCause().getMessage() != null) {
				response = ex.getCause().getMessage();
			} else {
				response = ex.getMessage();
			}
		}
		setState(model);
		model.addAttribute(MODEL_SERVER_HOST_ATTRIBUTE, serverHost);
		model.addAttribute(MODEL_SERVER_PORT_ATTRIBUTE, serverPort);
		model.addAttribute(MODEL_SERVER_PLATFORM_ATTRIBUTE, serverPlatform);
	}

	public void updateServerProperties(String server, String port, String platform, Model model) {
		initState();
		initServerSettings();
		Properties props = new Properties();
		try (FileInputStream in = new FileInputStream(CLAMAV_SERVICE_PROPERTIES_FILE)) {
			props.load(in);
			try (FileOutputStream out = new FileOutputStream(CLAMAV_SERVICE_PROPERTIES_FILE)) {
				props.setProperty(CLAMAV_SERVICE_HOST_PROPERTY, server);
				props.setProperty(CLAMAV_SERVICE_PORT_PROPERTY, port);
				props.setProperty(CLAMAV_SERVICE_PLATFORM_PROPERTY, platform);
				props.store(out, null);
				loadServerProperties();
				status = "success";
				response = "ClamAV service properies have been updated...";
			}
		} catch (IOException ex) {
			status = "error";
			if (ex.getCause().getMessage() != null) {
				response = ex.getCause().getMessage();
			} else {
				response = ex.getMessage();
			}
		}
		setState(model);
	}

	public void doPing(Model model) {
		initState();
		try {
			getClamavClient().ping();
			status = "success";
			response = "Pong";
		} catch (ClamavException | IOException ex) {
			status = "error";
			if (ex.getCause() instanceof UnresolvedAddressException) {
				response = "ClamAV service unavailable. Please check connection properties.";
			} else if (ex.getCause().getMessage() != null) {
				response = ex.getCause().getMessage();
			} else {
				response = ex.getMessage();
			}
		}
		setState(model);
	}

	public void getVersion(Model model) {
		initState();
		try {
			response = getClamavClient().version();
			status = "success";
		} catch (ClamavException | IOException ex) {
			status = "error";
			if (ex.getCause() instanceof UnresolvedAddressException) {
				response = "ClamAV service unavailable. Please check connection properties.";
			} else if (ex.getCause().getMessage() != null) {
				response = ex.getCause().getMessage();
			} else {
				response = ex.getMessage();
			}
		}
		setState(model);
	}

	public void doReloadVirusDatabases(Model model) {
		initState();
		try {
			getClamavClient().reloadVirusDatabases();
			status = "success";
			response = "Virus databases have been reloaded...";
		} catch (ClamavException | IOException ex) {
			status = "error";
			if (ex.getCause() instanceof UnresolvedAddressException) {
				response = "ClamAV service unavailable. Please check connection properties.";
			} else if (ex.getCause().getMessage() != null) {
				response = ex.getCause().getMessage();
			} else {
				response = ex.getMessage();
			}
		}
		setState(model);
	}

	public void getStats(Model model) {
		initState();
		try {
			response = getClamavClient().stats();
			status = "success";
		} catch (ClamavException | IOException ex) {
			status = "error";
			if (ex.getCause() instanceof UnresolvedAddressException) {
				response = "ClamAV service unavailable. Please check connection properties.";
			} else if (ex.getCause().getMessage() != null) {
				response = ex.getCause().getMessage();
			} else {
				response = ex.getMessage();
			}
		}
		setState(model);
	}

	public void doScanFolder(String path, Model model) {
		initState();
		if (path != null && !path.isBlank()) {
			try {
				ScanResult scanResult = getClamavClient().parallelScan(Paths.get(path));
				if (scanResult instanceof ScanResult.OK) {
					status = "success";
					response = "Viruses not found";
				} else if (scanResult instanceof ScanResult.VirusFound) {
					status = "Found viruses";
					response = ((ScanResult.VirusFound) scanResult).getFoundViruses().toString();
				}
			} catch (ClamavException | IOException ex) {
				status = "error";
				if (ex.getCause() instanceof UnresolvedAddressException) {
					response = "ClamAV service unavailable. Please check connection properties.";
				} else if (ex.getCause().getMessage() != null) {
					response = ex.getCause().getMessage();
				} else {
					response = ex.getMessage();
				}
			}
		}
		setState(model);
	}

	public void doScanFile(MultipartFile file, Model model) {
		initState();
		if (file != null && !file.isEmpty()) {
			try {
				ScanResult scanResult = getClamavClient().scan(file.getInputStream());
				if (scanResult instanceof ScanResult.OK) {
					status = "success";
					response = "Viruses not found";
				} else if (scanResult instanceof ScanResult.VirusFound) {
					status = "Found viruses";
					response = ((ScanResult.VirusFound) scanResult).getFoundViruses().toString();
				}
			} catch (ClamavException | IOException ex) {
				status = "error";
				if (ex.getCause() instanceof UnresolvedAddressException) {
					response = "ClamAV service unavailable. Please check connection properties.";
				} else if (ex.getCause().getMessage() != null) {
					response = ex.getCause().getMessage();
				} else {
					response = ex.getMessage();
				}
			}
		} else {
			status = "error";
			response = "File not found or empty";
		}
		setState(model);
	}
}
