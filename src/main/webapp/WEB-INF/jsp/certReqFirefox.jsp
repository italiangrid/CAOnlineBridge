<c:url var="saveUrl" value="/certReq/certReq" />
<form:form modelAttribute="certificateRequest" method="POST"
	action="${saveUrl }">
	<form:errors path="*" cssClass="errorblock" element="div" />
	<strong>Personal Informations:</strong>
	<div id="reset"></div>
	<div id="personalInfo">
		<table>
			<tr>
				<td><form:label path="mail">Mail: ${certificateRequest.mail }</form:label></td>
				<td><form:hidden path="mail" /></td>
			</tr>

			<tr>
				<td><form:label path="cn">CN:  ${certificateRequest.cn }</form:label></td>
				<td><form:hidden path="cn" /></td>
			</tr>
			
			<tr>
				<td><form:label path="o">O:  ${certificateRequest.o }</form:label></td>
				<td><form:hidden path="o" /></td>
			</tr>

			<tr>
				<td><form:label path="l">L:  ${certificateRequest.l }</form:label></td>
				<td><form:hidden path="l" /></td>
			</tr>
			
			<tr>
				<td><form:label path="l">C: IT</form:label></td>
				<td></td>
			</tr>
		</table>
	</div>
	<!-- <div id="contetRight">
		Your personal data retreived from your Identity Provider.
	</div> -->
	<div id="reset"  style="margin-bottom: 15px;"></div>
	<div style="display: none;">
	<strong>Key selection:</strong> 
	<div id="reset" ></div>
	<div id="keygen">
		<table>
			<tr>
				<td>Key strength:</td>
				<td><keygen id="spkac" name="spkac"
						challenge="TheChallenge1000" /></td>
			</tr>

			<tr>
				<td></td>
				<td><form:hidden path="proxyPass1" /></td>
			</tr>

			<tr>
				<td></td>
				<td><form:hidden path="proxyPass2" /></td>
			</tr>

		</table>
	</div>

	<div id="contetRight">
		Select the Key strength.
	</div>
	<div id="reset"></div>
	</div>
	<input type="submit" value="Get Certificate" onclick="loading();"/>
</form:form>