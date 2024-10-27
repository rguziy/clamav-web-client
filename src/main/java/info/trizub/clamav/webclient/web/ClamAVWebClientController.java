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

	@Autowired
	private ClamAVWebClientService webClientService;

	@GetMapping(value = { "", "/", "/home" })
	public String home(Model model) {
		return "home";
	}

	@GetMapping("/ping")
	public String ping(Model model) {
		webClientService.doPing(model);
		return "home";
	}

	@GetMapping("/version")
	public String version(Model model) {
		webClientService.getVersion(model);
		return "home";
	}

	@GetMapping("/stats")
	public String stats(Model model) {
		webClientService.getStats(model);
		return "home";
	}

	@GetMapping("/reload")
	public String reload(Model model) {
		webClientService.doReloadVirusDatabases(model);
		return "home";
	}

	@GetMapping("/scanFolder")
	public String scanFolder(Model model) {
		model.addAttribute("action", "scanFolder");
		return "home";
	}

	@PostMapping("/scanFolder")
	public String scanFolder(@RequestParam(required = false, defaultValue = "/scandir") String path, Model model) {
		webClientService.doScanFolder(path, model);
		model.addAttribute("action", "scanFolder");
		return "home";
	}

	@GetMapping("/scanFile")
	public String scanFile(Model model) {
		model.addAttribute("action", "scanFile");
		return "home";
	}

	@PostMapping("/scanFile")
	public String scanFile(@RequestParam MultipartFile file, Model model) {
		webClientService.doScanFile(file, model);
		model.addAttribute("action", "scanFile");
		return "home";
	}
}
