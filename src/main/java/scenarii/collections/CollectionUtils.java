package scenarii.collections;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Created by geoffrey on 18/07/2016.
 */
public class CollectionUtils {

    public static String join(List<String> list, String separator){
        StringBuffer buf = new StringBuffer();
        int size = list.size();
        int i = 0;
        for (String s : list){
            buf.append(s);
            if(i < size-1)
                buf.append(separator);
            i++;
        }
        return buf.toString();
    }

    public static String join(Collection<?> set , String separator){
        LinkedList<String> list = new LinkedList<>();
        set.forEach(o -> list.add(o.toString()));
        return join(list, separator);
    }
}
