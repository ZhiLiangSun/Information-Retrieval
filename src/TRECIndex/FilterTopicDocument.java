package TRECIndex;

import Utils.Path;
import Utils.FileUtils;
import Utils.Topic;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FilterTopicDocument {
    public List<String> getDocumentList(int querynumber, String property) {
        List<String> DocList = new ArrayList<>();
        BufferedReader br;
        String R_ListPath = Path.Data_Path + "/answer/list/" + querynumber + property;
        try {
            br = new BufferedReader(new FileReader(R_ListPath));
            String line = br.readLine();

            while (line != null) {
                DocList.add(line);
                line = br.readLine();
            }
            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return DocList;
    }


    public static void main(String[] args) {
        int[] topics = Topic.topics_all;

        List<String> RList, NRList;
        FilterTopicDocument FTD_R = new FilterTopicDocument();
        FilterTopicDocument FTD_NR = new FilterTopicDocument();

        for (int i = 0; i < topics.length; i++) {
            RList = FTD_R.getDocumentList(topics[i], "RList");
            NRList = FTD_NR.getDocumentList(topics[i], "NRList");
            for (String list : RList) {
                FileUtils.copy(Path.Data_Path + "/raw_parsed/" + list,
                        Path.Answer_Path + topics[i] + "/R/" + list);
            }
            for (String list : NRList) {
                FileUtils.copy(Path.Data_Path + "/raw_parsed/" + list,
                        Path.Answer_Path + topics[i] + "/NR/" + list);
            }
        }
    }
}
