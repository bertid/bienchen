package de.grumbelbart.bienchen;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParseFile {
	static final String path = "/Users/floh/Documents/code/bienchen/";

	static File find_latest_file() {
		File dir = new File(path);
		File files[] = dir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith("html") || name.toLowerCase().endsWith("htm");
			}
		});

		if (null == files || 0 == files.length)
			return null;

		Arrays.sort(files, 0, files.length, new Comparator<File>() {
			@Override
			public int compare(File o1, File o2) {
				return o1.getAbsolutePath().compareTo(o2.getAbsolutePath());
			}
		});

		return files[0];
	}

	static String read_file(File f) throws IOException {
		System.out.println("Reading file: " + f);

		BufferedReader br = new BufferedReader(new FileReader(f));

		String line;
		StringBuilder complete_file = new StringBuilder("");
		while (null != (line = br.readLine())) {
			complete_file.append(line);
			complete_file.append("\n");
		}

		return complete_file.toString();
	}

	static ArrayList<String> read_file_filtered(File f) throws IOException {
		System.out.println("Reading file: " + f);

		BufferedReader br = new BufferedReader(new FileReader(f));

		String line;
		ArrayList<String> content = new ArrayList<String>();
		boolean is_inside = false;
		while (null != (line = br.readLine())) {
			if (!is_inside && line.contains("ninja_table_row"))
				is_inside = true;

			if (is_inside) {
				if (line.contains("</tbody>"))
					break;
				else
					content.add(line);
			}
		}

		return content;
	}

	public static void main(String[] args) throws IOException {
		//String content = read_file(find_latest_file());

		// ninja_table_row_0
		// 
		//Pattern P = Pattern.compile("(ninja_table_row)");
		//Matcher m = P.matcher(content);
		//boolean matches = m.matches();
		//System.out.println("Group 0 = " + m.group(1));

		int sum_total = 0;
		int sum_voted = 0;

		ArrayList<String> ss = read_file_filtered(find_latest_file());

		HashMap<String, String[]> plz_to_data = new HashMap<String, String[]>();
		for (int i = 0; i < ss.size(); ++i) {
			if (ss.get(i).trim().startsWith("<td>")) {
				// <td>04/02/2019</td><td>97659</td><td>Rhön-Grabfeld</td><td>Schönau a.d.Brend</td><td>1024</td><td>33</td>    </tr>
				String parts[] = ss.get(i).replace("<td>", "").trim().split("</td>");

				try {
					String[] other = plz_to_data.get(parts[1]);
					if (null != other) {
						int voted_old = Integer.parseInt(other[5]);
						int voted_new = Integer.parseInt(parts[5]);
						if (voted_old > voted_new)
							continue;
					}
					plz_to_data.put(parts[1], parts);

				} catch (Exception e) {
				}
			}
		}

		for (Entry<String, String[]> entry : plz_to_data.entrySet()) {
			String[] parts = entry.getValue();

			String date = parts[0];
			String plz = parts[1];
			String name = parts[2];
			String name2 = parts[3];
			String total = parts[4];
			String voted = parts[5];

			try {
				int total_ = Integer.parseInt(total.trim());
				int voted_ = Integer.parseInt(voted.trim());

				sum_total += total_;
				sum_voted += voted_;
			} catch (Exception e) {
			}
		}

		System.out.println(
				"Total: " + sum_total + ", voted: " + sum_voted + ", ratio = " + (100.0 * sum_voted / sum_total));
	}
}
