import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class GetStillsFromUrls {

	public static void main(String[] args) {
		List<FileURL> downloadURLS = new ArrayList<FileURL>();
		boolean loop = true;
		String downloadLoc = System.getProperty("user.home") + File.separator + "Downloads";
		String url;
		String folder;
		String input;
		
		Scanner reader = new Scanner(System.in);
		System.out.println("High resolution images will be saved in: " + downloadLoc);
		while(loop){
			downloadLoc = System.getProperty("user.home") + File.separator + "Downloads";
			
			System.out.print("Enter the URL from an IMDB image result page: ");
			url = reader.nextLine();
			System.out.print("Enter a new folder name to download to (default is 'IMDB'): ");
			folder = reader.nextLine();
			
			if(folder.equals("") || folder == null){
				downloadLoc += File.separator + "IMDB";
			} else {
				downloadLoc += File.separator + folder;
			}
			File path = new File(downloadLoc);
			if(!path.exists())
				path.mkdirs();
			
			downloadURLS = parseHTML(url);
			
			downloadStills(downloadURLS, downloadLoc);
			System.out.println("Finished.");
			
			System.out.println("Enter another URL (Y/N)?");
			input = reader.nextLine();
			if(input.toUpperCase().equals("N")){
				loop = false;
				reader.close();
			}
		}
		System.out.println("Goodbye.");
	}
	
	public static List<String> readFile(String filename) {
		List<String> urls = new ArrayList<String>();
	    try {
	    	BufferedReader br = new BufferedReader(new FileReader(filename));
			
	        String line = br.readLine();

	        while (line != null) {
	            urls.add(line);
	            line = br.readLine();
	        }
	        br.close();
	    } catch (IOException e) {
			e.printStackTrace();
		}
	    return urls;
	}
	
	public static List<FileURL> parseHTML(String input){
		List<FileURL> downloadURLS = new ArrayList<FileURL>();
		boolean grabJPG = false;
		String url = input;
		
		while(url != null){
			String nextURL = null;
//			System.out.println(url+"\n");
			try {
				URL source = new URL(url);
				BufferedReader in = new BufferedReader(new InputStreamReader(source.openStream()));
				
				String inputLine = in.readLine();
				while (inputLine != null){
					if(inputLine.contains("class=\"prevnext\"") && inputLine.contains("Next")){
						String tmp = inputLine.replaceAll("<a href=\"", "");
						tmp = tmp.replaceAll("\" class=\"prevnext\" >Next&nbsp;&raquo;</a>", "");
						nextURL = "http://www.imdb.com" + tmp.trim();
					}
					if(inputLine.contains("media_index_thumbnail_grid")){
						grabJPG = true;
					}
					if(grabJPG && inputLine.contains("src=")){
						String tmp = inputLine.replaceAll("(src=|\")", "");
						tmp = tmp.replaceAll("V1_.+", "V1_.jpg");	//change small images to big
						FileURL furl = new FileURL(tmp);
						downloadURLS.add(furl);
					}
					if(grabJPG && inputLine.contains("</div>")){
						grabJPG = false;
						inputLine = null;
					} else {
						inputLine = in.readLine();
					}
				}
				
				in.close();
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			url = nextURL;
		}
		
		
		return downloadURLS;
	}
	public static void downloadStills(List<FileURL> downloadURLS, String downloadLoc){
		for(FileURL downloadURL : downloadURLS){
			System.out.println("Saving image " + (downloadURLS.indexOf(downloadURL)+1) + " out of " + downloadURLS.size());
			try {
				URL url = new URL(downloadURL.getUrl());
				InputStream in = url.openStream();
				Files.copy(in, Paths.get(downloadLoc + File.separator + downloadURL.getFilename()));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	

}
