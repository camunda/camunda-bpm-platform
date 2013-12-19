<xsl:stylesheet version="2.0" xmlns:saxon="http://icl.com/saxon"
							  xmlns:omgdi="http://www.omg.org/spec/DD/20100524/DI"
							  xmlns:omgdc="http://www.omg.org/spec/DD/20100524/DC"
							  xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"
							  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
							  xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
							  xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL"
							  >
	<xsl:strip-space elements="*"/>
	<xsl:output method="xml" omit-xml-declaration="no" saxon:indent-spaces="4" indent="yes"/>
	<!-- param to override the process engine id during signavio id replacement -->
	<xsl:param name="keepLanes">true</xsl:param> <!-- we might need to strip again for DI and offset-->

	<!-- we are only processing the first engine pool -->
	<xsl:variable name="enginePool" select="//bpmn:process[count(//bpmn:process)=1 or @isExecutable='true' or @name='Process Engine'][1]" />
	<xsl:variable name="enginePoolId" select="//bpmn:participant[@processRef=$enginePool/@id ]/@id" />

	<!--
		we will move the element to the top left position by using the lane position as offset
		the params will hold the position values
	-->
	<xsl:param name="offsetX">
		<xsl:choose>
			<xsl:when test="//bpmndi:BPMNShape[@bpmnElement=$enginePoolId]/omgdc:Bounds/@x">
				<xsl:value-of select="//bpmndi:BPMNShape[@bpmnElement=$enginePoolId]/omgdc:Bounds/@x"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="0.0"/>
			</xsl:otherwise>
	    </xsl:choose>
	</xsl:param>

	<xsl:param name="offsetY">
		<xsl:choose>
			<xsl:when test="//bpmndi:BPMNShape[@bpmnElement=$enginePoolId]/omgdc:Bounds/@y">
				<xsl:value-of select="//bpmndi:BPMNShape[@bpmnElement=$enginePoolId]/omgdc:Bounds/@y"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="0.0"/>
			</xsl:otherwise>
	    </xsl:choose>
	</xsl:param>

	<xsl:template match="/">
		<xsl:apply-templates select="bpmn:definitions"/>
	</xsl:template>

	<!-- this is the identity transform, we copy everything, including attributes which does not have a matching template -->
	<xsl:template match="*">
		<xsl:copy>
			<xsl:for-each select="@*">
				<xsl:copy />
			</xsl:for-each>
			<xsl:apply-templates />
		</xsl:copy>
	</xsl:template>

	<xsl:template match="bpmn:definitions">
		<xsl:copy>
			<xsl:for-each select="@*">
				<xsl:copy />
			</xsl:for-each>
			<!-- overwrite targetNamespace signavio / modelers might occupy it -->
			<xsl:attribute name="targetNamespace">
				<xsl:value-of select="'http://www.omg.org/spec/BPMN/20100524/MODEL'"></xsl:value-of>
			</xsl:attribute>

			<xsl:choose>
				<xsl:when test="$enginePool"> <!-- if we have an engine pool, continue -->
					<xsl:apply-templates />
				</xsl:when>
				<xsl:otherwise>
					<cycle>Could not detect an Engine Pool, be sure to set the "isExecutable" attribute to "true", or set the name of the Pool to "Process Engine".</cycle>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:copy>
	</xsl:template>

	<xsl:template match="bpmn:process[@id=$enginePool/@id]">
		<xsl:copy>
			<xsl:for-each select="@*">
				<xsl:copy />
			</xsl:for-each>

			<!-- engine pools are executable -->
			<xsl:attribute name="isExecutable">
				<xsl:value-of select="'true'"></xsl:value-of>
			</xsl:attribute>

			<xsl:apply-templates select="*"/>
		</xsl:copy>
	</xsl:template>

	<xsl:template match="bpmn:process[@id!=$enginePool/@id]" />

	<!-- exclude some elements from copy template -->
	<xsl:template match="bpmn:incoming" />
	<xsl:template match="bpmn:outgoing" />
	<xsl:template match="bpmn:messageFlow" />
	<xsl:template match="bpmn:participant[@processRef != $enginePool/@id or @id != $enginePoolId]" />

	<!-- just allow messageEventDefinitions where parent is a startEvent -->
	<xsl:template match="bpmn:laneSet[not($keepLanes ='true')]" />

  <!--
    do not ever exclude extension elements (need them for round-tripping back)
  -->
  <!--
	<xsl:template match="node()[namespace-uri() = 'http://www.signavio.com']" />
	<xsl:template match="bpmn:extensionElements[count(self::node()//*[namespace-uri() != 'http://www.signavio.com']) = 0]" />
  -->
	<xsl:template match="bpmn:association">
		<xsl:variable name="targetRef" select="./@targetRef" />
		<xsl:variable name="sourceRef" select="./@sourceRef" />
		<xsl:choose>
			<xsl:when test="count($enginePool//*[@id = $sourceRef]) > 0 and count($enginePool//*[@id = $targetRef]) > 0">
				<xsl:copy>
					<xsl:for-each select="@*">
						<xsl:copy />
					</xsl:for-each>

					<xsl:apply-templates />
				</xsl:copy>
			</xsl:when>
		</xsl:choose>
	</xsl:template>

	<xsl:template match="bpmndi:BPMNEdge[@bpmnElement = $enginePool//bpmn:association/@id]">
		<xsl:variable name="bpmnElement" select="./@bpmnElement" />
		<xsl:variable name="targetRef" select="$enginePool//bpmn:association[@id = $bpmnElement]/@targetRef" />
		<xsl:variable name="sourceRef" select="$enginePool//bpmn:association[@id = $bpmnElement]/@sourceRef" />

		<xsl:choose>
			<xsl:when test="count($enginePool//*[@id = $sourceRef]) > 0 and count($enginePool//*[@id = $targetRef]) > 0">
				<xsl:copy>
					<xsl:for-each select="@*">
						<xsl:copy />
					</xsl:for-each>

					<xsl:apply-templates />
				</xsl:copy>
			</xsl:when>
		</xsl:choose>

	</xsl:template>

	<xsl:template match="bpmndi:BPMNShape|bpmndi:BPMNEdge">
		<xsl:variable name="bpmnElement" select="./@bpmnElement" />

		<xsl:choose>
		<!-- we have to fix the ids in the DI too, see sequenceFlow logic-->
			<xsl:when test="//bpmn:participant[@id = $bpmnElement and @processRef=$enginePool/@id]/@id or $enginePool//*[@id = $bpmnElement][not(self::bpmn:lane and $keepLanes = 'false')]/@id">
				<xsl:copy>
					<xsl:for-each select="@*">
						<xsl:copy />
					</xsl:for-each>

					<xsl:apply-templates />
				</xsl:copy>
			</xsl:when>
		</xsl:choose>
	</xsl:template>

	<xsl:template match="omgdc:Bounds|omgdi:waypoint">
		<xsl:copy>
			<xsl:for-each select="@*">
				<xsl:copy />
			</xsl:for-each>
				<!-- move the elements according to the lane offset-->
				<xsl:attribute name="x">
					<xsl:value-of select="./@x - number($offsetX)" />
				</xsl:attribute>
				<xsl:attribute name="y">
					<xsl:value-of select="./@y - number($offsetY)" />
				</xsl:attribute>
			<xsl:apply-templates />
		</xsl:copy>
	</xsl:template>

</xsl:stylesheet>