DESCRIPTION = "Texas Instruments OpenMAX IL RAGECKO Decoder."
DEPENDS = "tidspbridge-lib tiopenmax-core tiopenmax-lcml tiopenmax-rmproxy tiopenmax-resourcemanager tiopenmax-audiomanager tiopenmax-rmparser tiopenmax-perf"
PR = "r0"
PACKAGES = "${PN}-dbg ${PN}-patterns ${PN}-dev ${PN}"

require tiopenmax-cspec-${PV}.inc
CCASE_PATHFETCH = "\
	/vobs/wtbu/OMAPSW_MPU/linux/audio/src/openmax_il/ragecko_dec \
	/vobs/wtbu/OMAPSW_MPU/linux/video/src/openmax_il/rm_parser/inc \
	/vobs/wtbu/OMAPSW_MPU/linux/Makefile \
	/vobs/wtbu/OMAPSW_MPU/linux/Master.mk \
	"
CCASE_PATHCOMPONENTS = 3
CCASE_PATHCOMPONENT = "linux"

SRC_URI = " \
        file://23.14-rageckodecnocore.patch;patch=1 \
        file://23.14-rageckodectestnocore.patch;patch=1 \
	${@base_contains("DISTRO_FEATURES", "testpatterns", "", "file://remove-patterns.patch;patch=1", d)} \
        "

inherit ccasefetch

do_compile_prepend() {
	install -d ${D}/usr/omx/patterns
	install -d ${D}/usr/lib
	install -d ${D}/usr/bin
	
}

do_compile() {
	cd ${S}/audio/src/openmax_il/ragecko_dec
	rm inc/TIDspOmx.h
        cp ${STAGING_INCDIR}/omx/TIDspOmx.h inc/
	oe_runmake \
		PREFIX=${D}/usr PKGDIR=${S} \
		CROSS=${AR%-*}- \
		BRIDGEINCLUDEDIR=${STAGING_INCDIR}/dspbridge BRIDGELIBDIR=${STAGING_LIBDIR} \
		TARGETDIR=${D}/usr OMXROOT=${S} OMXLIBDIR=${STAGING_LIBDIR} \
		OMXINCLUDEDIR=${STAGING_INCDIR}/omx \
		RAPARSERINCLUDEDIR=${S}/video/src/openmax_il/rm_parser/inc \
		OMX_PERF_INSTRUMENTATION=1 OMX_PERF_CUSTOMIZABLE=1 \
		all
}

do_install() {
	cd ${S}/audio/src/openmax_il/ragecko_dec
	oe_runmake \
		PREFIX=${D}/usr PKGDIR=${S} \
		CROSS=${AR%-*}- \
		BRIDGEINCLUDEDIR=${STAGING_INCDIR}/dspbridge BRIDGELIBDIR=${STAGING_LIBDIR} \
		TARGETDIR=${D}/usr OMXROOT=${S} \
		SYSTEMINCLUDEDIR=${D}/usr/include/omx \
		RAPARSERINCLUDEDIR=${S}/video/src/openmax_il/rm_parser/inc \
		OMX_PERF_INSTRUMENTATION=1 OMX_PERF_CUSTOMIZABLE=1 \
		install
}

do_stage() {
	cd ${S}/audio/src/openmax_il/ragecko_dec
	oe_runmake \
		PREFIX=${STAGING_DIR_TARGET}/usr PKGDIR=${S} \
		CROSS=${AR%-*}- \
		BRIDGEINCLUDEDIR=${STAGING_INCDIR}/dspbridge BRIDGELIBDIR=${STAGING_LIBDIR} \
		TARGETDIR=${STAGING_DIR_TARGET}/usr OMXROOT=${S} \
		OMX_PERF_INSTRUMENTATION=1 OMX_PERF_CUSTOMIZABLE=1 \
		SYSTEMINCLUDEDIR=${STAGING_INCDIR}/omx \
		install
}

FILES_${PN} = "\
	/usr/lib \
	/usr/bin \
	"

FILES_${PN}-patterns = "\
	/usr/omx/patterns \
	"

FILES_${PN}-dbg = "\
	/usr/bin/.debug \
	/usr/lib/.debug \
	"

FILES_${PN}-dev = "\
	/usr/include \
	"

do_stage_rm_omxdir() {
	# Clean up undesired staging
	${@base_contains("DISTRO_FEATURES", "testpatterns", "rm -rf ${STAGING_DIR_TARGET}/usr/omx/", "echo nothing to do here!", d)}
}

# with testpatterns enabled it does not work. Needs revision
do_install_cleanup() {
	${@base_contains("DISTRO_FEATURES", "testpatterns", "mv ${D}${bindir}/audio_decoder.ra_* ${D}/usr/omx/patterns", "echo nothing to do here!", d)}
	${@base_contains("DISTRO_FEATURES", "testpatterns", "mv ${D}${bindir}/cook*.rm ${D}/usr/omx/patterns", "echo nothing to do here!", d)}
}

addtask install_cleanup after do_install before do_package
addtask stage_rm_omxdir after do_populate_staging before do_package_stage
