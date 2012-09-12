<xsl:stylesheet version="2.0" xmlns:saxon="http://icl.com/saxon" 
							  xmlns:omgdi="http://www.omg.org/spec/DD/20100524/DI" 
							  xmlns:omgdc="http://www.omg.org/spec/DD/20100524/DC" 
							  xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI" 
							  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
							  xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL" 
							  xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL"
							  xmlns:activiti="http://activiti.org/bpmn"
							  >
	<xsl:strip-space elements="*"/>
	<xsl:output method="xml" omit-xml-declaration="no" saxon:indent-spaces="4" indent="yes"/>
	
	<xsl:param name="idReplaceStart">^[^a-zA-Z]</xsl:param>
	<xsl:param name="idReplaceStartWith">z</xsl:param>
	<xsl:param name="idReplace">[^a-zA-Z0-9-]</xsl:param>
	<xsl:param name="idReplaceWith">_</xsl:param>
	
	<xsl:param name="keepLanes">true</xsl:param>
	<xsl:param name="externalPoolId">Process_Engine</xsl:param>

	<xsl:variable name="enginePool" select="//bpmn:process[count(//bpmn:process)=1 or @isExecutable='true' or @name='Process Engine'][1]" />
	
	<xsl:param name="enginePoolId">
		<xsl:choose>
			<xsl:when test="starts-with($enginePool/@id, 'sid-')">
				<xsl:value-of select="$externalPoolId"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$enginePool/@id"/>
			</xsl:otherwise>
	    </xsl:choose>
	</xsl:param>
	
	<xsl:template match="bpmn:laneSet[not($keepLanes ='true')]" />
	
	<xsl:template match="@processType" />
	<xsl:template match="@name[.='']" />
	<xsl:template match="@implementation" />
	<xsl:template match="@startQuantity" />
	<xsl:template match="@isForCompensation[. = 'false']" />
	<xsl:template match="@completionQuantity" />
	<xsl:template match="@isInterrupting[. = 'false']" />
	<xsl:template match="@isClosed" />
	<xsl:template match="@isImmediate[. = 'false']" />
	
	<xsl:template match="/">
		<xsl:apply-templates select="bpmn:definitions"/>
	</xsl:template>
	
	<!-- this is the identity transform, we copy everything, including attributes which does not have a matching template -->
	<xsl:template match="node()|@*">
			<xsl:copy>
				<xsl:apply-templates select="node()|@*" />
			</xsl:copy>
	</xsl:template>
	
	<xsl:template name="create-ref">
		<xsl:param name="refId" />
		<xsl:choose>
			<xsl:when test="//*[@id=$refId]/@name != ''">
				<xsl:variable name="newId" select="concat(replace(replace(//*[@id=$refId]/@name, $idReplaceStart, $idReplaceStartWith), $idReplace, $idReplaceWith), '_', count(//*[@id=$refId]/preceding::*) + 1)" />
					<xsl:choose>
					   <xsl:when test="count(//*[@id = $newId]) = 0">
							<xsl:value-of select="$newId"/>
					   </xsl:when>
					   <xsl:otherwise>
                           <xsl:call-template name="counterLoop">
                              <xsl:with-param name="newId" select="$newId"></xsl:with-param>
                              <xsl:with-param name="counter" select="2"></xsl:with-param>
                           </xsl:call-template>
					   </xsl:otherwise>
					</xsl:choose>
			</xsl:when>
			<xsl:otherwise>
                <xsl:variable name="newId" select="concat(local-name(//*[@id=$refId]), '_', count(//*[@id=$refId]/preceding::*) + 1)" />
                    <xsl:choose>
                       <xsl:when test="count(//*[@id = $newId]) = 0">
                            <xsl:value-of select="$newId"/>
                       </xsl:when>
                       <xsl:otherwise>
                           <xsl:call-template name="counterLoop">
                              <xsl:with-param name="newId" select="$newId"></xsl:with-param>
                              <xsl:with-param name="counter" select="2"></xsl:with-param>
                           </xsl:call-template>
                       </xsl:otherwise>
                    </xsl:choose>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template name="counterLoop">
	   <xsl:param name="newId" />
	   <xsl:param name="counter" />
	   <xsl:choose>
 	      <xsl:when test="count(//*[@id = concat($newId, '_', $counter)]) = 0">
 	          <xsl:value-of select="concat($newId, '_', $counter)"/>
 	      </xsl:when>
 	      <xsl:otherwise>
 	          <xsl:call-template name="counterLoop">
 	              <xsl:with-param name="newId" select="$newId"></xsl:with-param>
 	              <xsl:with-param name="counter" select="$counter + 1"></xsl:with-param>
 	          </xsl:call-template>
 	      </xsl:otherwise>     
 	   </xsl:choose>
	</xsl:template>
	
	<xsl:template match="bpmn:definitions">
		<xsl:copy>
			<xsl:for-each select="@*[not(local-name()='schemaLocation')]"> <!-- schemaLocation interferes with Activiti parser -->
				<xsl:copy />
			</xsl:for-each>
		
			<xsl:apply-templates select="bpmn:*" />
			<xsl:apply-templates select="bpmndi:*" />
		</xsl:copy>
	</xsl:template>
	
	<xsl:template match="bpmn:process[@id=$enginePool/@id]">
		<xsl:copy>
			<xsl:apply-templates select="@*"/>
			
			<!-- engine pools are executable -->
			<xsl:attribute name="isExecutable">
				<xsl:value-of select="'true'"></xsl:value-of>
			</xsl:attribute>
			
			<xsl:attribute name="id">
				<xsl:value-of select="$enginePoolId"/>
			</xsl:attribute>

			<xsl:apply-templates select="*"/>
		</xsl:copy>
	</xsl:template>
	
	<xsl:template match="@processRef[. = $enginePool/@id]">
		<xsl:attribute name="processRef">
			<xsl:value-of select="$enginePoolId"/>
		</xsl:attribute>
	</xsl:template>
	
	<xsl:template match="@bpmnElement[. = $enginePool/@id]">
		<xsl:attribute name="bpmnElement">
			<xsl:value-of select="$enginePoolId"/>
		</xsl:attribute>
	</xsl:template>
	
	<xsl:template match="@id[starts-with(., 'sid-') and not(. = $enginePool/@id) ]|@sourceRef[starts-with(., 'sid-')]|@targetRef[starts-with(., 'sid-')]|@default[starts-with(., 'sid-')]|@processRef[starts-with(., 'sid-') and not(. = $enginePool/@id)]|@messageRef[starts-with(., 'sid-')]|@bpmnElement[starts-with(., 'sid-') and not(. = $enginePool/@id)]|@attachedToRef[starts-with(., 'sid-') and not(. = $enginePool/@id)]|@dataStoreRef[starts-with(., 'sid-') and not(. = $enginePool/@id)]|@signalRef[starts-with(., 'sid-')]|@errorRef[starts-with(., 'sid-')]">
        <xsl:attribute name="{local-name()}">
		<xsl:call-template name="create-ref">
			<xsl:with-param name="refId" select="." />
		</xsl:call-template>
		</xsl:attribute>
	</xsl:template>
	
	<xsl:template match="bpmn:flowNodeRef[starts-with(text(), 'sid-')]|bpmn:incoming[starts-with(text(), 'sid-')]|bpmn:outgoing[starts-with(text(), 'sid-')]|bpmn:sourceRef[starts-with(text(), 'sid-')]|bpmn:targetRef[starts-with(text(), 'sid-')]">
	    <xsl:element name="{local-name()}">
		<xsl:call-template name="create-ref">
			<xsl:with-param name="refId" select="." />
		</xsl:call-template>
		</xsl:element>
	</xsl:template>
	
</xsl:stylesheet>