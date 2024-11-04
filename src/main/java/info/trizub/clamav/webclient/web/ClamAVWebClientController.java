/**
 * Class Name: ClamAVWebClientController
 * Description: This class handles the HTTP requests.
 * 
 * Author: Ruslan Huzii
 * Email:  ruslan@trizub.info
 * Date: 2024-11-04
 * 
 * License: GNU Lesser General Public License v2.1
 */
package info.trizub.clamav.webclient.web;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import info.trizub.clamav.webclient.service.ClamAVWebClientService;
import info.trizub.clamav.webclient.service.ClamAVWebClientService.Request;

@Controller
public class ClamAVWebClientController {

	@Autowired
	private ClamAVWebClientService webClientService;

	private final static String MAIN_PAGE = "main";
	private final static String MODEL_ACTION_ATTRIBUTE = "action";

	public enum Action {
		SETTINGS("settings"), UPDATE_RESULT("update-result"), PING("ping"), VERSION("version"), STATS("stats"),
		RELOAD("reload"), SCAN_FOLDER("scanFolder"), SCAN_FOLDER_RESULT("scanFolder-result"), SCAN_FILE("scanFile"),
		SCAN_FILE_RESULT("scanFile-result");

		private String value;

		Action(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}
	}

	@GetMapping(value = { "", "/", "/settings", "/main" })
	public String settings(Model model) {
		webClientService.requestHandler(Request.GET_SERVICE_PROPERTIES, null, model);
		model.addAttribute(MODEL_ACTION_ATTRIBUTE, Action.SETTINGS.getValue());
		return MAIN_PAGE;
	}

	@PostMapping("/update")
	public String update(@RequestParam(required = true) String host, @RequestParam(required = true) String port,
			@RequestParam(required = true) String platform, Model model) {
		webClientService.requestHandler(Request.UPDATE_SERVICE_PROPERTIES,
				Map.of("server", host, "port", port, "platform", platform), model);
		model.addAttribute(MODEL_ACTION_ATTRIBUTE, Action.UPDATE_RESULT.getValue());
		return MAIN_PAGE;
	}

	@GetMapping("/ping")
	public String ping(Model model) {
		webClientService.requestHandler(Request.GET_PING, null, model);
		model.addAttribute(MODEL_ACTION_ATTRIBUTE, Action.PING.getValue());
		return MAIN_PAGE;
	}

	@GetMapping("/version")
	public String version(Model model) {
		webClientService.requestHandler(Request.GET_VERSION, null, model);
		model.addAttribute(MODEL_ACTION_ATTRIBUTE, Action.VERSION.getValue());
		return MAIN_PAGE;
	}

	@GetMapping("/stats")
	public String stats(Model model) {
		webClientService.requestHandler(Request.GET_STATS, null, model);
		model.addAttribute(MODEL_ACTION_ATTRIBUTE, Action.STATS.getValue());
		return MAIN_PAGE;
	}

	@GetMapping("/reload")
	public String reload(Model model) {
		webClientService.requestHandler(Request.RELOAD_VIRUS_DATABASES, null, model);
		model.addAttribute(MODEL_ACTION_ATTRIBUTE, Action.RELOAD.getValue());
		return MAIN_PAGE;
	}

	@GetMapping("/scanFolder")
	public String scanFolder(Model model) {
		webClientService.requestHandler(Request.GET_SCAN_FOLDER, null, model);
		model.addAttribute(MODEL_ACTION_ATTRIBUTE, Action.SCAN_FOLDER.getValue());
		return MAIN_PAGE;
	}

	@PostMapping("/scanFolder")
	public String scanFolder(@RequestParam(required = true) String path, Model model) {
		webClientService.requestHandler(Request.SCAN_FOLDER, path, model);
		model.addAttribute(MODEL_ACTION_ATTRIBUTE, Action.SCAN_FOLDER_RESULT.getValue());
		return MAIN_PAGE;
	}

	@GetMapping("/scanFile")
	public String scanFile(Model model) {
		webClientService.requestHandler(Request.GET_SCAN_FILE, null, model);
		model.addAttribute(MODEL_ACTION_ATTRIBUTE, Action.SCAN_FILE.getValue());
		return MAIN_PAGE;
	}

	@PostMapping("/scanFile")
	public String scanFile(@RequestParam MultipartFile file, Model model) {
		webClientService.requestHandler(Request.SCAN_FILE, file, model);
		model.addAttribute(MODEL_ACTION_ATTRIBUTE, Action.SCAN_FILE_RESULT.getValue());
		return MAIN_PAGE;
	}

	@GetMapping("/setLocale")
	public String setLocale(@RequestParam String lang, Model model) {
		webClientService.requestHandler(Request.UPDATE_LANGUAGE, lang, model);
		return settings(model);
	}

}
