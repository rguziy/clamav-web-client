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

	private final static String CLAMAV_WEB_CLIENT_PROPERTIES_FILE = "conf/clamav-web-client.properties";
	private final static String CLAMAV_SERVICE_HOST_PROPERTY = "clamav.service.host";
	private final static String CLAMAV_SERVICE_PORT_PROPERTY = "clamav.service.port";
	private final static String CLAMAV_SERVICE_PLATFORM_PROPERTY = "clamav.service.platform";
	private final static String CLAMAV_CLIENT_SCAN_FOLDER_PROPERTY = "clamav.client.scan.folder";
	private final static String CLAMAV_CLIENT_LANGUAGE_PROPERTY = "clamav.client.language";

	public final static String MODEL_STATUS_ATTRIBUTE = "status";
	public final static String MODEL_RESPONSE_ATTRIBUTE = "response";
	private final static String MODEL_SERVICE_HOST_ATTRIBUTE = "host";
	private final static String MODEL_SERVICE_PORT_ATTRIBUTE = "port";
	private final static String MODEL_SERVICE_PLATFORM_ATTRIBUTE = "platform";
	private final static String MODEL_SERVICE_SCAN_FOLDER_ATTRIBUTE = "scanFolder";

	private final static String RESPONSE_PONG_MESSAGE = "web.main.response.pong";
	private final static String RESPONSE_VIRUSES_NOT_FOUND_MESSAGE = "web.main.response.viruses.not.found";
	private final static String RESPONSE_CONNECTION_PROBLEM_MESSAGE = "web.main.response.connection.problem";
	private final static String RESPONSE_PROPERTIES_LOADED_MESSAGE = "web.main.response.properties.loaded";
	private final static String RESPONSE_PROPERTIES_UPDATED_MESSAGE = "web.main.response.properties.updated";
	private final static String RESPONSE_DATABASES_RELOADED_MESSAGE = "web.main.response.databases.reloaded";

	public enum Request {
		GET_VERSION, GET_STATS, GET_SERVICE_PROPERTIES, GET_SCAN_FOLDER, GET_PING, GET_LANGUAGE, RELOAD_VIRUS_DATABASES,
		SCAN_FOLDER, SCAN_FILE, UPDATE_SERVICE_PROPERTIES, UPDATE_LANGUAGE
	}

	public enum Status {
		SUCCESS("success"), FAILED("failed"), VIRUSES_FOUND("viruses-found");

		private String value;

		Status(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}
	}

	private ClamavClient client;
	private String status;
	private String response;
	private String serviceHost;
	private String servicePort;
	private String servicePlatform;
	private String clientScanFolder;
	private String clientLanguage;

	public ClamAVWebClientService() {
		initState();
		initProperties();
	}

	private void initState() {
		status = null;
		response = null;
	}

	private void setState(Model model) {
		if (model != null) {
			model.addAttribute(MODEL_STATUS_ATTRIBUTE, status);
			model.addAttribute(MODEL_RESPONSE_ATTRIBUTE, response);
		}
	}

	private void initProperties() {
		serviceHost = null;
		servicePort = null;
		servicePlatform = null;
		clientScanFolder = null;
		clientLanguage = null;
		client = null;
	}

	private ClamavClient getClamavClient() throws IOException {
		if (client == null) {
			loadProperties();
			Platform platform = null;
			if (servicePlatform != null) {
				if (servicePlatform.equalsIgnoreCase(Platform.UNIX.name())) {
					platform = Platform.UNIX;
				} else if (servicePlatform.equalsIgnoreCase(Platform.WINDOWS.name())) {
					platform = Platform.WINDOWS;
				} else if (servicePlatform.equalsIgnoreCase(Platform.WINDOWS.name())) {
					platform = Platform.JVM_PLATFORM;
				}
			}
			if (platform != null) {
				client = new ClamavClient(serviceHost, Integer.parseInt(servicePort), platform);
			} else {
				client = new ClamavClient(serviceHost, Integer.parseInt(servicePort));
			}
		}
		return client;
	}

	private void loadProperties() throws IOException {
		initProperties();
		Properties props = new Properties();
		try (FileInputStream in = new FileInputStream(CLAMAV_WEB_CLIENT_PROPERTIES_FILE)) {
			props.load(in);
			if (props != null && !props.isEmpty()) {
				if (props.containsKey(CLAMAV_SERVICE_HOST_PROPERTY)) {
					serviceHost = props.getProperty(CLAMAV_SERVICE_HOST_PROPERTY);
				}
				if (props.containsKey(CLAMAV_SERVICE_PORT_PROPERTY)) {
					servicePort = props.getProperty(CLAMAV_SERVICE_PORT_PROPERTY);
				}
				if (props.containsKey(CLAMAV_SERVICE_PLATFORM_PROPERTY)) {
					servicePlatform = props.getProperty(CLAMAV_SERVICE_PLATFORM_PROPERTY);
				}
				if (props.containsKey(CLAMAV_CLIENT_SCAN_FOLDER_PROPERTY)) {
					clientScanFolder = props.getProperty(CLAMAV_CLIENT_SCAN_FOLDER_PROPERTY);
				}
				if (props.containsKey(CLAMAV_CLIENT_LANGUAGE_PROPERTY)) {
					clientLanguage = props.getProperty(CLAMAV_CLIENT_LANGUAGE_PROPERTY);
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
		Properties props = null;
		try {
			switch (request) {
			case GET_SERVICE_PROPERTIES:
				if (serviceHost == null || servicePort == null || servicePlatform == null) {
					loadProperties();
				}
				model.addAttribute(MODEL_SERVICE_HOST_ATTRIBUTE, serviceHost);
				model.addAttribute(MODEL_SERVICE_PORT_ATTRIBUTE, servicePort);
				model.addAttribute(MODEL_SERVICE_PLATFORM_ATTRIBUTE, servicePlatform);
				status = Status.SUCCESS.getValue();
				response = getMessage(RESPONSE_PROPERTIES_LOADED_MESSAGE);
				break;
			case UPDATE_SERVICE_PROPERTIES:
				try (FileInputStream in = new FileInputStream(CLAMAV_WEB_CLIENT_PROPERTIES_FILE)) {
					props = new Properties();
					props.load(in);
					try (FileOutputStream out = new FileOutputStream(CLAMAV_WEB_CLIENT_PROPERTIES_FILE)) {
						props.setProperty(CLAMAV_SERVICE_HOST_PROPERTY, ((Map<String, String>) input).get("server"));
						props.setProperty(CLAMAV_SERVICE_PORT_PROPERTY, ((Map<String, String>) input).get("port"));
						props.setProperty(CLAMAV_SERVICE_PLATFORM_PROPERTY,
								((Map<String, String>) input).get("platform"));
						props.store(out, null);
					}
				}
				loadProperties();
				status = Status.SUCCESS.getValue();
				response = getMessage(RESPONSE_PROPERTIES_UPDATED_MESSAGE);
				break;
			case GET_LANGUAGE:
				if (clientLanguage == null || clientLanguage.isBlank()) {
					loadProperties();
				}
				status = Status.SUCCESS.getValue();
				response = clientLanguage;
				break;
			case UPDATE_LANGUAGE:
				clientLanguage = (String) input;
				props = new Properties();
				try (FileInputStream in = new FileInputStream(CLAMAV_WEB_CLIENT_PROPERTIES_FILE)) {
					props.load(in);
					try (FileOutputStream out = new FileOutputStream(CLAMAV_WEB_CLIENT_PROPERTIES_FILE)) {
						props.setProperty(CLAMAV_CLIENT_LANGUAGE_PROPERTY, clientLanguage);
						props.store(out, null);
					}
				}
				break;
			case GET_PING:
				getClamavClient().ping();
				status = Status.SUCCESS.getValue();
				response = getMessage(RESPONSE_PONG_MESSAGE);
				break;
			case GET_VERSION:
				response = getClamavClient().version();
				status = Status.SUCCESS.getValue();
				break;
			case RELOAD_VIRUS_DATABASES:
				getClamavClient().reloadVirusDatabases();
				status = Status.SUCCESS.getValue();
				response = getMessage(RESPONSE_DATABASES_RELOADED_MESSAGE);
				break;
			case GET_STATS:
				response = getClamavClient().stats();
				status = Status.SUCCESS.getValue();
				break;
			case GET_SCAN_FOLDER:
				if (clientScanFolder == null || clientScanFolder.isBlank()) {
					loadProperties();
				}
				model.addAttribute(MODEL_SERVICE_SCAN_FOLDER_ATTRIBUTE, clientScanFolder);
				break;
			case SCAN_FOLDER:
				scanResult = getClamavClient().parallelScan(Paths.get((String) input));
				if (scanResult instanceof ScanResult.OK) {
					status = Status.SUCCESS.getValue();
					response = getMessage(RESPONSE_VIRUSES_NOT_FOUND_MESSAGE);
				} else if (scanResult instanceof ScanResult.VirusFound) {
					status = Status.VIRUSES_FOUND.getValue();
					response = ((ScanResult.VirusFound) scanResult).getFoundViruses().toString();
				}
				clientScanFolder = (String) input;
				props = new Properties();
				try (FileInputStream in = new FileInputStream(CLAMAV_WEB_CLIENT_PROPERTIES_FILE)) {
					props.load(in);
					try (FileOutputStream out = new FileOutputStream(CLAMAV_WEB_CLIENT_PROPERTIES_FILE)) {
						props.setProperty(CLAMAV_CLIENT_SCAN_FOLDER_PROPERTY, clientScanFolder);
						props.store(out, null);
					}
				}
				break;
			case SCAN_FILE:
				scanResult = getClamavClient().scan(((MultipartFile) input).getInputStream());
				if (scanResult instanceof ScanResult.OK) {
					status = Status.SUCCESS.getValue();
					response = getMessage(RESPONSE_VIRUSES_NOT_FOUND_MESSAGE);
				} else if (scanResult instanceof ScanResult.VirusFound) {
					status = Status.VIRUSES_FOUND.getValue();
					response = ((ScanResult.VirusFound) scanResult).getFoundViruses().toString();
				}
				break;
			default:
				break;
			}
		} catch (ClamavException | IOException ex) {
			status = Status.FAILED.getValue();
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
