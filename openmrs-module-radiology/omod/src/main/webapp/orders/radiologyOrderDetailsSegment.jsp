<%@ include file="/WEB-INF/template/include.jsp"%>

<c:if test="${not empty radiologyOrder}">
    <table>
      <tr>
        <td><spring:message code="radiology.radiologyOrder.accessionNumber" /></td>
        <td>${radiologyOrder.accessionNumber}</td>
      </tr>
      <tr>
        <td><spring:message code="radiology.imagingProcedure" /></td>
        <td>${radiologyOrder.concept.name.name}</td>
      </tr>
      <tr>
        <td><spring:message code="radiology.radiologyOrder.orderReason" /></td>
        <td><c:if test="${not empty radiologyOrder.orderReason}">${radiologyOrder.orderReason.name.name}</c:if></td>
      </tr>
      <tr>
        <td><spring:message code="radiology.radiologyOrder.orderReasonNonCoded" /></td>
        <td>${radiologyOrder.orderReasonNonCoded}</td>
      </tr>
      <tr>
        <td><spring:message code="radiology.radiologyOrder.clinicalHistory" /></td>
        <td>${radiologyOrder.clinicalHistory}</td>
      </tr>
      <tr>
        <td><spring:message code="Order.orderer" /></td>
        <td> ${radiologyOrder.orderer.name}</td>
      </tr>
      <tr>
        <td><spring:message code="radiology.urgency" /></td>
        <td><spring:message code="radiology.order.urgency.${radiologyOrder.urgency}" text="${radiologyOrder.urgency}" /></td>
      </tr>
      <tr>
        <td><spring:message code="radiology.scheduledDate" /></td>
        <td class="datetime">${radiologyOrder.effectiveStartDate}</td>
      </tr>
      <tr>
        <td><spring:message code="radiology.stopDate" /></td>
        <td class="datetime">${radiologyOrder.effectiveStopDate}</td>
      </tr>
      <tr>
        <td><spring:message code="radiology.performedStatus" /></td>
        <td><spring:message code="radiology.${radiologyOrder.study.performedStatus}" text="${radiologyOrder.study.performedStatus}" />
        </td>
      </tr>
      <tr>
        <td><spring:message code="general.instructions" /></td>
        <td>${radiologyOrder.instructions}</td>
      </tr>
      <tr>
        <td><spring:message code="general.createdBy" /></td>
        <td>${radiologyOrder.creator.personName}-<span class="datetime"> ${radiologyOrder.dateCreated} </span></td>
      </tr>
      <openmrs:hasPrivilege privilege="Get Radiology Studies">
        <c:if test="${not empty dicomViewerUrl}">
          <tr>
            <td><spring:message code="radiology.studyResults" /></td>
            <td><a href="${dicomViewerUrl}" target="_tab">View Study</a></td>
          </tr>
        </c:if>
      </openmrs:hasPrivilege>
    </table>
</c:if>