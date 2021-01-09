package fr.gear.cousteau;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Translation
{	
	private static final String DEFAULT_TRANSLATION_FOLDER = "locales";

	private static final TranslationFileFilter filter = new TranslationFileFilter();
	
	private Map<String, Map<String, String>> translation;
	private String currentLanguage;
	private String fallbackLanguage;

	public Translation() throws IOException
	{
		this(Translation.DEFAULT_TRANSLATION_FOLDER);
	}
		
	public Translation(String translationFolderPath) throws IOException
	{
		this(translationFolderPath, Locale.getDefault().getLanguage());
	}

	public Translation(String translationFolderPath, String fallbackLanguage) throws IOException
	{
		this(translationFolderPath, Locale.getDefault().getLanguage(), fallbackLanguage);
	}

	public Translation(String translationFolderPath, String defaultLanguage, String fallbackLanguage) throws IOException
	{
		this.translation = new HashMap<String, Map<String, String>>();
		this.setLangague(defaultLanguage);
		this.loadTranslation(translationFolderPath);
	}

	private void loadTranslation(String translationFolderPath) throws IOException
	{
		File translationFolder = new File(translationFolderPath);
		if(translationFolder.isDirectory())
		{
			this.parseTranslationFolderContent(translationFolder);
		}
		else
		{
			throw new IOException("The translation folder path (" + translationFolderPath + ") is not a directory");
		}
	}

	public void setLangague(String language)
	{
		this.currentLanguage = language;
	}
	
	public void setFallback(String language)
	{
		this.fallbackLanguage = language;
	}

	public String translate(String key)
	{
		Map<String, String> translationForCurrentLanguage = this.translation.get(this.currentLanguage);

		if(translationForCurrentLanguage == null)
		{
			translationForCurrentLanguage = this.translation.get(this.fallbackLanguage);
		}
		
		if(translationForCurrentLanguage != null && translationForCurrentLanguage.containsKey(key))
		{
			return translationForCurrentLanguage.get(key);
		}
		else
		{
			return key;
		}
	}

	private void parseTranslationFolderContent(File translationFolder) throws IOException
	{
		for(File file : translationFolder.listFiles(filter))
		{
			if(file.isFile())
			{
				this.parseTranslationFile(file);
			}
		}
	}

	private void parseTranslationFile(File file) throws IOException
	{
		TranslationFile translationFile = new TranslationFile(file);
		this.translation.put(translationFile.getLanguage(), translationFile.asMap());
	}
	
	public static class TranslationFileFilter implements FilenameFilter
	{
		private static final String TRANSLATION_FILE_EXTENSION = ".lang";
		
		@Override
		public boolean accept(File parent, String name)
		{
			return name.endsWith(TRANSLATION_FILE_EXTENSION);
		}
		
	}

	public static class TranslationFile
	{
		private static final String KEY_VALUE_SEPARATOR = "=";
		
		private File file;
		private Map<String, String> translation;

		public TranslationFile(String path) throws IOException
		{
			this(new File(path));
		}

		public TranslationFile(File file) throws IOException
		{
			this.file = file;
			this.translation = new HashMap<String, String>();
			this.parse();
		}

		public String getLanguage()
		{
			return this.file.getName().split("\\.")[0];
		}
		
		public Map<String, String> asMap()
		{			
			return this.translation;
		}
		
		private void parse() throws IOException
		{
			List<String> lines = Files.readAllLines(Paths.get(this.file.getPath()));
			for(int i = 0; i < lines.size(); i++)
			{
				String line = lines.get(i);
				String[] keyValue = line.split(KEY_VALUE_SEPARATOR);
				if(keyValue.length == 2)
				{
					this.translation.put(keyValue[0], keyValue[1]);
				}
				else
				{
					throw new IOException("Incorrect line n°" + i + ", cannot parse : " + line);
				}
			}
		}
	}
}
