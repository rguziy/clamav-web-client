package info.trizub.clamav.webclient.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import info.trizub.clamav.webclient.service.ClamAVWebClientService;

@Controller
public class ClamAVWebClientController {

	private final static String MODEL_ACTION_ATTRIBUTE = "action";

	@Autowired
	private ClamAVWebClientService webClientService;

	@GetMapping(value = { "", "/", "/home" })
	public String home(Model model) {
		webClientService.setServerProperties(model);
		model.addAttribute(MODEL_ACTION_ATTRIBUTE, "home");
		return "home";
	}

	@PostMapping("/update")
	public String update(@RequestParam(required = true) String host, @RequestParam(required = true) String port,
			@RequestParam(required = true) String platform, Model model) {
		webClientService.updateServerProperties(host, port, platform, model);
		model.addAttribute(MODEL_ACTION_ATTRIBUTE, "update-result");
		return "home";
	}

	@GetMapping("/ping")
	public String ping(Model model) {
		webClientService.doPing(model);
		model.addAttribute(MODEL_ACTION_ATTRIBUTE, "ping");
		return "home";
	}

	@GetMapping("/version")
	public String version(Model model) {
		webClientService.getVersion(model);
		model.addAttribute(MODEL_ACTION_ATTRIBUTE, "version");
		return "home";
	}

	@GetMapping("/stats")
	public String stats(Model model) {
		webClientService.getStats(model);
		model.addAttribute(MODEL_ACTION_ATTRIBUTE, "stats");
		return "home";
	}

	@GetMapping("/reload")
	public String reload(Model model) {
		webClientService.doReloadVirusDatabases(model);
		model.addAttribute(MODEL_ACTION_ATTRIBUTE, "reload");
		return "home";
	}

	@GetMapping("/scanFolder")
	public String scanFolder(Model model) {
		model.addAttribute(MODEL_ACTION_ATTRIBUTE, "scanFolder");
		return "home";
	}

	@PostMapping("/scanFolder")
	public String scanFolder(@RequestParam(required = true) String path, Model model) {
		webClientService.doScanFolder(path, model);
		model.addAttribute(MODEL_ACTION_ATTRIBUTE, "scanFolder-result");
		return "home";
	}

	@GetMapping("/scanFile")
	public String scanFile(Model model) {
		model.addAttribute(MODEL_ACTION_ATTRIBUTE, "scanFile");
		return "home";
	}

	@PostMapping("/scanFile")
	public String scanFile(@RequestParam MultipartFile file, Model model) {
		webClientService.doScanFile(file, model);
		model.addAttribute(MODEL_ACTION_ATTRIBUTE, "scanFile-result");
		return "home";
	}
}
