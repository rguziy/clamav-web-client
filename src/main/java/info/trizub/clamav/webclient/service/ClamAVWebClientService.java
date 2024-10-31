package info.trizub.clamav.webclient.service;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.UnresolvedAddressException;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;

import xyz.capybara.clamav.ClamavClient;
import xyz.capybara.clamav.ClamavException;
import xyz.capybara.clamav.Platform;
import xyz.capybara.clamav.commands.scan.result.ScanResult;

@Service
public class ClamAVWebClientService {

	@Autowired
	private MessageSource messageSource;

	private final static String CLAMAV_SERVICE_PROPERTIES_FILE = "conf/clamav-service.properties";
	private final static String CLAMAV_SERVICE_HOST_PROPERTY = "clamav.service.host";
	private final static String CLAMAV_SERVICE_PORT_PROPERTY = "clamav.service.port";
	private final static String CLAMAV_SERVICE_PLATFORM_PROPERTY = "clamav.service.platform";

	private final static String MODEL_STATUS_ATTRIBUTE = "status";
	private final static String MODEL_RESPONSE_ATTRIBUTE = "response";
	private final static String MODEL_SERVER_HOST_ATTRIBUTE = "host";
	private final static String MODEL_SERVER_PORT_ATTRIBUTE = "port";
	private final static String MODEL_SERVER_PLATFORM_ATTRIBUTE = "platform";

	private final static String RESPONSE_PONG_MESSAGE = "web.main.response.pong";
	private final static String RESPONSE_VIRUSES_NOT_FOUND_MESSAGE = "web.main.response.viruses.not.found";
	private final static String RESPONSE_CONNECTION_PROBLEM_MESSAGE = "web.main.response.connection.problem";
	private final static String RESPONSE_PROPERTIES_LOADED_MESSAGE = "web.main.response.properties.loaded";
	private final static String RESPONSE_PROPERTIES_UPDATED_MESSAGE = "web.main.response.properties.updated";
	private final static String RESPONSE_DATABASES_RELOADED_MESSAGE = "web.main.response.databases.reloaded";

	private final static String STATUS_VIRUSES_FOUND_MESSAGE = "web.main.status.viruses.found";
	private final static String STATUS_SUCCESS_MESSAGE = "web.main.status.success";
	private final static String STATUS_ERROR_MESSAGE = "web.main.status.error";

	public enum Request {
		DO_PING, GET_VERSION, DO_RELOAD_VIRUS_DATABASES, GET_STATS, DO_SCAN_FOLDER, DO_SCAN_FILE, SET_SERVER_PROPERTIES,
		UPDATE_SERVER_PROPERTIES
	}

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

	public String getMessage(String message, @Nullable Object[] params) {
		Locale locale = LocaleContextHolder.getLocale();
		return messageSource.getMessage(message, params, locale);
	}

	public String getMessage(String message) {
		return getMessage(message, null);
	}

	@SuppressWarnings("unchecked")
	public void requestHandler(Request request, Object input, Model model) {
		initState();
		ScanResult scanResult = null;
		try {
			switch (request) {
			case SET_SERVER_PROPERTIES:
				loadServerProperties();
				status = getMessage(STATUS_SUCCESS_MESSAGE);
				response = getMessage(RESPONSE_PROPERTIES_LOADED_MESSAGE);
				model.addAttribute(MODEL_SERVER_HOST_ATTRIBUTE, serverHost);
				model.addAttribute(MODEL_SERVER_PORT_ATTRIBUTE, serverPort);
				model.addAttribute(MODEL_SERVER_PLATFORM_ATTRIBUTE, serverPlatform);
				break;
			case UPDATE_SERVER_PROPERTIES:
				initServerSettings();
				Properties props = new Properties();
				try (FileInputStream in = new FileInputStream(CLAMAV_SERVICE_PROPERTIES_FILE)) {
					props.load(in);
					try (FileOutputStream out = new FileOutputStream(CLAMAV_SERVICE_PROPERTIES_FILE)) {
						props.setProperty(CLAMAV_SERVICE_HOST_PROPERTY, ((Map<String, String>) input).get("server"));
						props.setProperty(CLAMAV_SERVICE_PORT_PROPERTY, ((Map<String, String>) input).get("port"));
						props.setProperty(CLAMAV_SERVICE_PLATFORM_PROPERTY,
								((Map<String, String>) input).get("platform"));
						props.store(out, null);
						loadServerProperties();
						status = getMessage(STATUS_SUCCESS_MESSAGE);
						response = getMessage(RESPONSE_PROPERTIES_UPDATED_MESSAGE);
					}
				}
				break;
			case DO_PING:
				getClamavClient().ping();
				status = getMessage(STATUS_SUCCESS_MESSAGE);
				response = getMessage(RESPONSE_PONG_MESSAGE);
				break;
			case GET_VERSION:
				response = getClamavClient().version();
				status = getMessage(STATUS_SUCCESS_MESSAGE);
				break;
			case DO_RELOAD_VIRUS_DATABASES:
				getClamavClient().reloadVirusDatabases();
				status = getMessage(STATUS_SUCCESS_MESSAGE);
				response = getMessage(RESPONSE_DATABASES_RELOADED_MESSAGE);
				break;
			case GET_STATS:
				response = getClamavClient().stats();
				status = getMessage(STATUS_SUCCESS_MESSAGE);
				break;
			case DO_SCAN_FOLDER:
				scanResult = getClamavClient().parallelScan(Paths.get((String) input));
				if (scanResult instanceof ScanResult.OK) {
					status = getMessage(STATUS_SUCCESS_MESSAGE);
					response = getMessage(RESPONSE_VIRUSES_NOT_FOUND_MESSAGE);
				} else if (scanResult instanceof ScanResult.VirusFound) {
					status = getMessage(STATUS_VIRUSES_FOUND_MESSAGE);
					response = ((ScanResult.VirusFound) scanResult).getFoundViruses().toString();
				}
				break;
			case DO_SCAN_FILE:
				scanResult = getClamavClient().scan(((MultipartFile) input).getInputStream());
				if (scanResult instanceof ScanResult.OK) {
					status = getMessage(STATUS_SUCCESS_MESSAGE);
					response = getMessage(RESPONSE_VIRUSES_NOT_FOUND_MESSAGE);
				} else if (scanResult instanceof ScanResult.VirusFound) {
					status = getMessage(STATUS_VIRUSES_FOUND_MESSAGE);
					response = ((ScanResult.VirusFound) scanResult).getFoundViruses().toString();
				}
				break;
			default:
				break;
			}
		} catch (ClamavException | IOException ex) {
			status = getMessage(STATUS_ERROR_MESSAGE);
			if (ex.getCause() instanceof UnresolvedAddressException) {
				response = getMessage(RESPONSE_CONNECTION_PROBLEM_MESSAGE);
			} else if (ex.getCause().getMessage() != null) {
				response = ex.getCause().getMessage();
			} else {
				response = ex.getMessage();
			}
		}
		setState(model);
	}

}
