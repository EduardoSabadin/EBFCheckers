package utilsGUI;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Config {
	private String fileName = "./config/config.ini";
	private Path pathFileName = Paths.get(fileName);

	public Config() {
		// Cria a pasta config, caso não exista:
		(new File("./config")).mkdirs();

		if (Files.notExists(this.pathFileName)) {
			try {
				Files.createFile(this.pathFileName);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	public String loadConfig(String key, String defaultValue) {
		int separatorPosition;
		String keyConfig;
		String keyValue = "";
		boolean found = false;

		try {
			// Abrir o arquivo:
			List<String> linhas = Files.readAllLines(pathFileName);

			// Procurar a key:
			for (int i = 0; i < linhas.size(); i++) {
				String linha = linhas.get(i);
				separatorPosition = linha.indexOf("=");
				keyConfig = linha.substring(0, separatorPosition).toUpperCase();
				keyValue = linha.substring(separatorPosition + 1);

				// Se encontrar a chave:
				if (keyConfig.equals(key.toUpperCase())) {
					found = true;
					break;
				}
			}

			if (!found) {
				// Retorna o valor default:
				keyValue = defaultValue;
			}

		} catch (IOException e) {
			System.err.println("Erro ao ler o arquivo: " + e.getMessage());
		}

		return keyValue;
	}

	private void saveConfig(String key, String value) {
		try {
			// Abrir o arquivo:
			List<String> linhas = Files.readAllLines(pathFileName);
			int separatorPosition;
			String keyConfig;
			boolean changed = false;

			// Procurar a key:
			for (int i = 0; i < linhas.size(); i++) {
				String linha = linhas.get(i);
				separatorPosition = linha.indexOf("=");
				keyConfig = linha.substring(0, separatorPosition).toUpperCase();

				// Se encontrar substitui o valor dela:
				if (keyConfig.equals(key.toUpperCase())) {
					String newLine = keyConfig + "=" + value;
					linhas.set(i, newLine);
					changed = true;
					break;
				}
			}

			if (changed) {
				// Salva a lista alterada no arquivo de volta:
				Files.write(this.pathFileName, linhas);
			} else {
				// Se não encontrar: cria a key e o respectivo valor:
				linhas.add(key + "=" + value);
				Files.write(this.pathFileName, linhas);
			}
		} catch (IOException e) {
			System.err.println("Erro ao ler o arquivo: " + e.getMessage());
		}
	}

	public String getConfig(String key, String defaultValue) {
		String result = this.loadConfig(key, defaultValue);

		return result;
	}

	public int getConfig(String key, int defaultValue) {
		String result = this.loadConfig(key, String.valueOf(defaultValue));

		return Integer.valueOf(result);
	}

	public boolean getConfig(String key, boolean defaultValue) {
		String result = this.loadConfig(key, String.valueOf(defaultValue));

		return Boolean.valueOf(result);
	}

	public void setConfig(String key, String value) {
		this.saveConfig(key, value);
	}

	public void setConfig(String key, int value) {
		this.saveConfig(key, String.valueOf(value));
	}

	public void setConfig(String key, boolean value) {
		this.saveConfig(key, String.valueOf(value));
	}
}