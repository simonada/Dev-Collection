/** Processing JSON Example**/

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;

public HashMap < String, Integer > processContentDistributionResponse(String jsonResponse) throws Exception {
    Utils.info("Search API response " + jsonResponse);

    HashMap < String, Integer > contentVolume = new HashMap < String, Integer > ();

    ObjectMapper om = new ObjectMapper();
    JsonNode root = om.readTree(jsonResponse);

    JsonNode rootResultNode = root.get("result");
    JsonNode rootArrayStart = rootResultNode.get("distributions");

    if (rootArrayStart == null) {
        Utils.error("No contents returned");
        return null;
    }

    Iterator < JsonNode > contentIterator = rootArrayStart.getElements();
    while (contentIterator.hasNext()) {
        JsonNode contentNode = contentIterator.next();

        JsonNode distributions = contentNode.get("distributions");
        Iterator < JsonNode > valuesIterator = distributions.getElements();
        while (valuesIterator.hasNext()) {
            JsonNode valueNode = valuesIterator.next();
            JsonNode value = valueNode.get("value");
            JsonNode count = valueNode.get("count");
            contentVolume.put(value.getTextValue(), count.getIntValue());
        }
    }

    return contentVolume;
}