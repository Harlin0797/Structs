package it.fb.structs.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Flavio
 */
public class AbstractParser<M> {

    private final List<? extends MethodPattern<? super M>> methodPatterns;
    private final String name;
    private final Map<String, ParsedField> fields = new LinkedHashMap<String, ParsedField>();

    protected AbstractParser(String name, List<? extends MethodPattern<? super M>> methodPatterns) {
        this.name = name;
        this.methodPatterns = methodPatterns;
    }

    protected final ParsedField addMethod(M method) {
        for (MethodPattern<? super M> pattern : methodPatterns) {
            ParsedField pField = pattern.match(method);
            if (pField != null) {
                ParsedField prevField = fields.get(pField.getName());
                if (prevField != null) {
                    pField = prevField.mergeWith(pField);
                }
                fields.put(pField.getName(), pField);
                return pField;
            }
        }
        throw new ParseException("Unrecognized method signature: " + method);
    }

    protected final PStructDesc build() {
        List<ParsedField> fList = new ArrayList<ParsedField>(fields.values());
        for (ParsedField field : fList) {
            if (!field.isComplete()) {
                throw new ParseException("Field " + field + " is not complete (misses getter or setter)");
            }
        }
        Collections.sort(fList, ParsedField.PositionComparator);
        return new PStructDesc(name, fList);
    }

    protected interface MethodPattern<M> {
        public ParsedField match(M method);
    }

}
