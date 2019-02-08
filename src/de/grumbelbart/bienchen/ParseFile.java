package de.grumbelbart.bienchen;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map.Entry;

public class ParseFile {
    static final String path1 = "/Users/floh/Documents/code/bienchen/";
    static final String path2 = "h:/temp/bienchen/bienchen/";
    static final String path3 = "K:/P_bravo/bienchen/bienchen/";

    static File[] find_data_files() {
        File dir = new File(path1);
        if (!dir.exists())
            dir = new File(path2);
        if (!dir.exists())
            dir = new File(path3);

        File files[] = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith("html") || name.toLowerCase().endsWith("htm");
            }
        });

        if (null == files)
            files = new File[0];

        Arrays.sort(files, 0, files.length, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return -o1.getAbsolutePath().compareTo(o2.getAbsolutePath());
            }
        });

        return files;
    }

    static File find_latest_file() {
        File[] files = find_data_files();
        if (files.length == 0)
            return null;

        return files[0];
    }

    static String read_file(File f) throws IOException {
        System.out.println("Reading file: " + f);

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            StringBuilder complete_file = new StringBuilder("");
            while (null != (line = br.readLine())) {
                complete_file.append(line);
                complete_file.append("\n");
            }

            return complete_file.toString();
        }

    }

    static ArrayList<String> read_file_filtered(File f) throws IOException {
        System.out.println("Reading file: " + f);

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
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
    }

    static void analyze_file(File f) throws IOException {
        int sum_total = 0;
        int sum_voted = 0;

        ArrayList<String> ss = read_file_filtered(f);

        HashMap<String, String[]> plz_to_data = new HashMap<String, String[]>();
        int duplicates = 0;
        for (int i = 0; i < ss.size(); ++i) {
            if (ss.get(i).trim().startsWith("<td>")) {
                // <td>04/02/2019</td><td>97659</td><td>Rhön-Grabfeld</td><td>Schönau a.d.Brend</td><td>1024</td><td>33</td>    </tr>
                String parts[] = ss.get(i).replace("<td>", "").trim().split("</td>");
                if (parts.length < 6)
                    continue;

                // Remove the dot that separates thousands
                // Others use a space to separate
                parts[4] = parts[4].replace(".", "").replace(" ", "").trim();
                parts[5] = parts[5].replace(".", "").replace(" ", "").trim();

                if (parts[4].isEmpty() || parts[5].isEmpty())
                    continue;

                String FullName = parts[1] + parts[2] + parts[3];

                try {
                    String[] other = plz_to_data.get(FullName);
                    if (null != other) {
                        duplicates++;
                        int voted_old = Integer.parseInt(other[5]);
                        int voted_new = Integer.parseInt(parts[5]);
                        if (voted_old > voted_new) {
                            continue;
                        }
                    }
                    plz_to_data.put(FullName, parts);

                } catch (Exception e) {
                }
            }
        }

        int good_entries = 0;
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
                good_entries++;
            } catch (Exception e) {
                //System.out.println("Cannot: <" + total + ">, <" + voted + ">");
            }
        }

        System.out.println("Insgesamt " + good_entries + " Gemeinden (" + duplicates + " Duplikate), mit " + sum_total
                        + " Stimmberechtigten, Unterschrieben haben: " + sum_voted + ", ratio = "
                        + (100.0 * sum_voted / sum_total));
    }

    public static void main(String[] args) throws IOException {
        File[] data_files = find_data_files();
        for (File f : data_files)
            analyze_file(f);
    }
}
