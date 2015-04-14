<p:declare-step version='1.0' name="main"
                xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                xmlns:cxu="http://xmlcalabash.com/ns/extensions/xmlunit"
                exclude-inline-prefixes="c cxu">
<p:output port="result"/>

<!--
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
-->

<p:import href="../../../resources/library.xpl"/>

<cxu:compare name="compare">
  <p:input port="source">
    <p:inline><document>
<title>Some Title</title>
<para>Some paragraph.</para>
<para>Some other paragraph.</para>
</document></p:inline>
  </p:input>
  <p:input port="alternate">
    <p:inline><document>
<title>Some Title</title>
<para>Some paragraph.</para>
<para>Some other paragraph.</para>
</document></p:inline>
  </p:input>
</cxu:compare>

<p:choose>
  <p:xpath-context>
    <p:pipe step="compare" port="result"/>
  </p:xpath-context>
  <p:when test="/c:result = 'true'">
    <p:identity>
      <p:input port="source">
        <p:inline><c:result>PASS</c:result></p:inline>
      </p:input>
    </p:identity>
  </p:when>
  <p:otherwise>
    <p:error code="FAIL">
      <p:input port="source">
        <p:inline><message>Did not find expected text.</message></p:inline>
      </p:input>
    </p:error>
  </p:otherwise>
</p:choose>

</p:declare-step>
