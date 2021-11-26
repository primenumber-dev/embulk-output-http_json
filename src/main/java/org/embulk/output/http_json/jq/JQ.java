package org.embulk.output.http_json.jq;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.thisptr.jackson.jq.BuiltinFunctionLoader;
import net.thisptr.jackson.jq.JsonQuery;
import net.thisptr.jackson.jq.Scope;
import net.thisptr.jackson.jq.Version;
import net.thisptr.jackson.jq.Versions;
import net.thisptr.jackson.jq.exception.JsonQueryException;
import net.thisptr.jackson.jq.module.loaders.BuiltinModuleLoader;

public class JQ {

    private static final Version defaultVersion = Versions.JQ_1_6;
    private final Scope scope;
    private final Version version;

    public JQ(Scope scope, Version version) {
        this.scope = scope;
        this.version = version;
        initializeScope(version, scope);
    }

    public JQ() {
        this(Scope.newEmptyScope(), defaultVersion);
    }

    protected void initializeScope(Version version, Scope scope) {
        BuiltinFunctionLoader.getInstance().loadFunctions(version, scope);
        scope.setModuleLoader(BuiltinModuleLoader.getInstance());
    }

    public void validateFilter(String filter) throws InvalidJQFilterException {
        try {
            compile(filter);
        } catch (JsonQueryException e) {
            throw new InvalidJQFilterException(String.format("Invalid jq filter: %s", filter), e);
        }
    }

    private JsonQuery compile(String filter) throws JsonQueryException {
        return JsonQuery.compile(filter, version);
    }

    public List<JsonNode> jq(String filter, JsonNode input) throws IllegalJQProcessingException {
        final List<JsonNode> resultBuilder = new ArrayList<>();
        try {
            compile(filter).apply(scope, input, resultBuilder::add);
        } catch (JsonQueryException e) {
            throw new IllegalJQProcessingException(
                    String.format("Cannot process by the jq filter: %s", filter), e);
        }
        return Collections.unmodifiableList(resultBuilder);
    }
}
