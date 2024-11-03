package info.trizub.clamav.webclient.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.UnresolvedAddressException;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import jakarta.annotation.PostConstruct;
import xyz.capybara.clamav.ClamavClient;
import xyz.capybara.clamav.ClamavException;
import xyz.capybara.clamav.Platform;
import xyz.capybara.clamav.commands.scan.result.ScanResult;

@Service
public class ClamAVWebClientService {

	private static final Logger logger = LoggerFactory.getLogger(ClamAVWebClientService.class);

	@Autowired
	private MessageSource messageSource;

	@Autowired
	private SessionLocaleResolver localeResolver;

	private final static String DEFAULT_CLAMAV_HOST = "localhost";
	private final static String DEFAULT_CLAMAV_PORT = "3310";
	private final static Platform DEFAULT_CLAMAV_PLATFORM = Platform.UNIX;
	private final static String DEFAULT_CLIENT_LANGUAGE = "en";
	private final static String DEFAULT_CLIENT_SCAN_FOLDER = "/scandir";

	private final static String CLAMAV_WEB_CLIENT_PROPERTIES_FILE = "conf/clamav-web-client.properties";
	private final static String CLAMAV_SERVICE_HOST_PROPERTY = "clamav.service.host";
	private final static String CLAMAV_SERVICE_PORT_PROPERTY = "clamav.service.port";
	private final static String CLAMAV_SERVICE_PLATFORM_PROPERTY = "clamav.service.platform";
	private final static String CLAMAV_CLIENT_SCAN_FOLDER_PROPERTY = "clamav.client.scan.folder";
	private final static String CLAMAV_CLIENT_LANGUAGE_PROPERTY = "clamav.client.language";

	public final static String MODEL_STATUS_ATTRIBUTE = "status";
	public final static String MODEL_RESPONSE_ATTRIBUTE = "response";
	public final static String MODEL_LANGUAGE_ATTRIBUTE = "language";
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
		GET_VERSION, GET_STATS, GET_SERVICE_PROPERTIES, GET_SCAN_FOLDER, GET_SCAN_FILE, GET_PING, GET_LANGUAGE,
		RELOAD_VIRUS_DATABASES, SCAN_FOLDER, SCAN_FILE, UPDATE_SERVICE_PROPERTIES, UPDATE_LANGUAGE
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

	private String clamavHostVariable;
	private String clamavPortVariable;
	private Properties clamavWebClientProperties;
	private ClamavClient client;
	private String status;
	private String response;
	private String serviceHost;
	private String servicePort;
	private Platform servicePlatform;
	private String clientScanFolder;
	private String clientLanguage;

	public ClamAVWebClientService() {
		initConfiguration();
	}

	@PostConstruct
	private void updateDefaultLocale() {
		if (localeResolver != null && clientLanguage != null) {
			localeResolver.setDefaultLocale(new Locale(clientLanguage));
		}
	}

	private void initConfiguration() {
		clamavWebClientProperties = new Properties();
		File propertyFile = new File(CLAMAV_WEB_CLIENT_PROPERTIES_FILE);
		try {
			if (propertyFile.exists()) {
				try (FileInputStream in = new FileInputStream(propertyFile)) {
					clamavWebClientProperties.load(in);
				}
			} else {
				File parent = propertyFile.getParentFile();
				if (parent != null && !parent.exists()) {
					parent.mkdirs();
				}
				try (FileWriter writer = new FileWriter(propertyFile)) {
					clamavWebClientProperties.store(writer, null);
				}
			}
		} catch (IOException ex) {
			logger.error(ex.getMessage());
		}
		clamavHostVariable = System.getenv("CLAMAV_HOST");
		clamavPortVariable = System.getenv("CLAMAV_PORT");
		if (clamavHostVariable != null && !clamavHostVariable.isBlank()) {
			serviceHost = clamavHostVariable;
		} else if (clamavWebClientProperties.containsKey(CLAMAV_SERVICE_HOST_PROPERTY)) {
			serviceHost = clamavWebClientProperties.getProperty(CLAMAV_SERVICE_HOST_PROPERTY);
		}
		if (clamavPortVariable != null && !clamavPortVariable.isBlank()) {
			servicePort = clamavPortVariable;
		} else if (clamavWebClientProperties.containsKey(CLAMAV_SERVICE_PORT_PROPERTY)) {
			servicePort = clamavWebClientProperties.getProperty(CLAMAV_SERVICE_PORT_PROPERTY);
		}
		if (clamavWebClientProperties.containsKey(CLAMAV_SERVICE_PLATFORM_PROPERTY)) {
			servicePlatform = getPlatformByName(
					clamavWebClientProperties.getProperty(CLAMAV_SERVICE_PLATFORM_PROPERTY));
		}
		if (clamavWebClientProperties.containsKey(CLAMAV_CLIENT_LANGUAGE_PROPERTY)) {
			clientLanguage = clamavWebClientProperties.getProperty(CLAMAV_CLIENT_LANGUAGE_PROPERTY);
		}
		if (clamavWebClientProperties.containsKey(CLAMAV_CLIENT_SCAN_FOLDER_PROPERTY)) {
			clientScanFolder = clamavWebClientProperties.getProperty(CLAMAV_CLIENT_SCAN_FOLDER_PROPERTY);
		}
		if (serviceHost == null || serviceHost.isBlank()) {
			serviceHost = DEFAULT_CLAMAV_HOST;
		}
		if (servicePort == null || servicePort.isBlank()) {
			servicePort = DEFAULT_CLAMAV_PORT;
		}
		if (servicePlatform == null) {
			servicePlatform = DEFAULT_CLAMAV_PLATFORM;
		}
		if (clientLanguage == null) {
			clientLanguage = DEFAULT_CLIENT_LANGUAGE;
		}
		if (clientScanFolder == null) {
			clientScanFolder = DEFAULT_CLIENT_SCAN_FOLDER;
		}
		updateProperties();
	}

	private void updateProperties() {
		try (FileInputStream in = new FileInputStream(CLAMAV_WEB_CLIENT_PROPERTIES_FILE)) {
			clamavWebClientProperties = new Properties();
			clamavWebClientProperties.load(in);
			try (FileOutputStream out = new FileOutputStream(CLAMAV_WEB_CLIENT_PROPERTIES_FILE)) {
				clamavWebClientProperties.setProperty(CLAMAV_SERVICE_HOST_PROPERTY, serviceHost);
				clamavWebClientProperties.setProperty(CLAMAV_SERVICE_PORT_PROPERTY, servicePort);
				clamavWebClientProperties.setProperty(CLAMAV_SERVICE_PLATFORM_PROPERTY, servicePlatform.name());
				clamavWebClientProperties.setProperty(CLAMAV_CLIENT_LANGUAGE_PROPERTY, clientLanguage);
				clamavWebClientProperties.setProperty(CLAMAV_CLIENT_SCAN_FOLDER_PROPERTY, clientScanFolder);
				clamavWebClientProperties.store(out, null);
			}
		} catch (IOException ex) {
			logger.error(ex.getMessage());
		}
		client = new ClamavClient(serviceHost, Integer.parseInt(servicePort), servicePlatform);
	}

	private void initState() {
		status = null;
		response = null;
	}

	private void setState(Model model) {
		if (model != null) {
			model.addAttribute(MODEL_STATUS_ATTRIBUTE, status);
			model.addAttribute(MODEL_RESPONSE_ATTRIBUTE, response);
			model.addAttribute(MODEL_LANGUAGE_ATTRIBUTE, clientLanguage);
		}
	}

	private Platform getPlatformByName(String name) {
		Platform platform = DEFAULT_CLAMAV_PLATFORM;
		if (name != null && !name.isBlank()) {
			if (name.equalsIgnoreCase(Platform.UNIX.name())) {
				platform = Platform.UNIX;
			} else if (name.equalsIgnoreCase(Platform.WINDOWS.name())) {
				platform = Platform.WINDOWS;
			} else if (name.equalsIgnoreCase(Platform.WINDOWS.name())) {
				platform = Platform.JVM_PLATFORM;
			}
		}
		return platform;
	}

	private ClamavClient getClamavClient() throws IOException {
		if (client == null) {
			client = new ClamavClient(serviceHost, Integer.parseInt(servicePort), servicePlatform);
		}
		return client;
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
			case GET_SERVICE_PROPERTIES:
				model.addAttribute(MODEL_SERVICE_HOST_ATTRIBUTE, serviceHost);
				model.addAttribute(MODEL_SERVICE_PORT_ATTRIBUTE, servicePort);
				model.addAttribute(MODEL_SERVICE_PLATFORM_ATTRIBUTE, servicePlatform);
				status = Status.SUCCESS.getValue();
				response = getMessage(RESPONSE_PROPERTIES_LOADED_MESSAGE);
				break;
			case UPDATE_SERVICE_PROPERTIES:
				serviceHost = ((Map<String, String>) input).get("server");
				servicePort = ((Map<String, String>) input).get("port");
				servicePlatform = getPlatformByName(((Map<String, String>) input).get("platform"));
				updateProperties();
				status = Status.SUCCESS.getValue();
				response = getMessage(RESPONSE_PROPERTIES_UPDATED_MESSAGE);
				break;
			case GET_LANGUAGE:
				status = Status.SUCCESS.getValue();
				response = clientLanguage;
				break;
			case UPDATE_LANGUAGE:
				clientLanguage = (String) input;
				updateProperties();
				updateDefaultLocale();
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
				updateProperties();
				break;
			case GET_SCAN_FILE:
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
			logger.error(ex.getMessage());
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
