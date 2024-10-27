package info.trizub.clamav.webclient.service;

import java.io.IOException;
import java.nio.file.Paths;

import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;

import xyz.capybara.clamav.ClamavClient;
import xyz.capybara.clamav.ClamavException;
import xyz.capybara.clamav.Platform;
import xyz.capybara.clamav.commands.scan.result.ScanResult;

@Service
public class ClamAVWebClientService {

	private ClamavClient client = null;

	private String status = null;
	private String result = null;

	public ClamAVWebClientService() {
		client = new ClamavClient("192.168.1.189", 3310, Platform.UNIX);
	}

	public void doPing(Model model) {
		try {
			client.ping();
			status = "success";
			result = "Pong";
		} catch (ClamavException ex) {
			status = "error";
			result = ex.getCause().getMessage();
		}
		model.addAttribute("status", status);
		model.addAttribute("result", result);
	}

	public void getVersion(Model model) {
		try {
			result = client.version();
			status = "success";
		} catch (ClamavException ex) {
			status = "error";
			result = ex.getCause().getMessage();
		}
		model.addAttribute("status", status);
		model.addAttribute("result", result);
	}

	public void doReloadVirusDatabases(Model model) {
		try {
			client.reloadVirusDatabases();
			status = "success";
			result = "Virus databases have been reloaded...";
		} catch (ClamavException ex) {
			status = "error";
			result = ex.getCause().getMessage();
		}
		model.addAttribute("status", status);
		model.addAttribute("result", result);
	}

	public void getStats(Model model) {
		try {
			result = client.stats();
			status = "success";
		} catch (ClamavException ex) {
			status = "error";
			result = ex.getCause().getMessage();
		}
		model.addAttribute("status", status);
		model.addAttribute("result", result);
	}

	public void doScanFolder(String path, Model model) {
		if (path != null && !path.isBlank()) {
			try {
				ScanResult scanResult = client.parallelScan(Paths.get(path));
				if (scanResult instanceof ScanResult.OK) {
					status = "success";
					result = "Viruses not found";
				} else if (scanResult instanceof ScanResult.VirusFound) {
					status = "Found viruses";
					result = ((ScanResult.VirusFound) scanResult).getFoundViruses().toString();
				}
			} catch (ClamavException ex) {
				status = "error";
				result = ex.getCause().getMessage();
			}
		}
		model.addAttribute("status", status);
		model.addAttribute("result", result);
	}

	public void doScanFile(MultipartFile file, Model model) {
		if (file != null && !file.isEmpty()) {
			try {
				ScanResult scanResult = client.scan(file.getInputStream());
				if (scanResult instanceof ScanResult.OK) {
					status = "success";
					result = "Viruses not found";
				} else if (scanResult instanceof ScanResult.VirusFound) {
					status = "Found viruses";
					result = ((ScanResult.VirusFound) scanResult).getFoundViruses().toString();
				}
			} catch (ClamavException | IOException ex) {
				status = "error";
				result = ex.getCause().getMessage();
			}
		} else {
			status = "error";
			result = "File not found or empty";
		}
		model.addAttribute("status", status);
		model.addAttribute("result", result);
	}
}
