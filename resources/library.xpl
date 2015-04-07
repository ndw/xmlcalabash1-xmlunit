<p:library xmlns:p="http://www.w3.org/ns/xproc"
           xmlns:cxu="http://xmlcalabash.com/ns/extensions/xmlunit"
           version="1.0">

<p:declare-step type="cxu:compare">
   <p:input port="source" primary="true"/>
   <p:input port="alternate"/>
   <p:output port="result" primary="false"/>
   <p:option name="compare-unmatched" select="'false'"/>
   <p:option name="ignore-comments" select="'false'"/>
   <p:option name="ignore-diff-between-text-and-cdata" select="'false'"/>
   <p:option name="ignore-whitespace" select="'false'"/>
   <p:option name="normalize" select="'false'"/>
   <p:option name="normalize-whitespace" select="'false'"/>
   <p:option name="fail-if-not-equal" select="'false'"/>
</p:declare-step>

</p:library>
