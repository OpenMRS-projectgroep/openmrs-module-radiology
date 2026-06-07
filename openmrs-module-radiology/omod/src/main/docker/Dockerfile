FROM teleivo/openmrs-platform:2.0.0-1

# Get radiology modules dependencies
RUN curl -L \
    "https://openmrs.jfrog.io/openmrs/omods/omod/legacyui-1.2.omod" \
    -o "${OPENMRS_MODULES}/legacyui-1.2.omod"
RUN curl -k -L \
    "https://mavenrepo.openmrs.org/nexus/content/repositories/public/org/openmrs/module/webservices.rest-omod/2.24.0/webservices.rest-omod-2.24.0.jar" \
    -o "${OPENMRS_MODULES}/webservices.rest-2.24.0.omod"

COPY maven/*.omod ${OPENMRS_MODULES}/
