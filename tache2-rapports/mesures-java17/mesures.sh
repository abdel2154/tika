#!/bin/bash
# (bash, depuis la racine du depot ; necessite un 'mvn -pl tika-core -am install -DskipTests' prealable)
# Etapes : 1 = tests originaux ; 2 = + tests generes passe 1 ; 3 = + tests generes passe 2 ; 4 = + tests manuels
# Mesures PIT + JaCoCo en 4 etapes (Java 17). Lancer depuis C:/Users/roust/tika
set -x
OUT=tache2-rapports/mesures-java17
mkdir -p $OUT
M="mvn -B -o -ntp -pl tika-core -Dcheckstyle.skip -Drat.skip"
pit() { $M -Ptache2 test-compile org.pitest:pitest-maven:mutationCoverage "-DexcludedTestClasses=$2" > $OUT/pit-$1.log 2>&1; echo "PIT $1 exit $?"; rm -rf $OUT/pit-$1; mkdir -p $OUT/pit-$1; cp -r tika-core/target/pit-reports/* $OUT/pit-$1/; grep "Generated .* mutations" $OUT/pit-$1.log; }
jac() { rm -f tika-core/target/jacoco.exec; $M test "-Dtest=$2" -Dsurefire.failIfNoSpecifiedTests=false > $OUT/jacoco-$1.log 2>&1; echo "JACOCO $1 exit $?"; grep -E "Tests run:.*Skipped" $OUT/jacoco-$1.log | tail -1; rm -rf $OUT/jacoco-$1; mkdir -p $OUT/jacoco-$1; cp tika-core/target/site/jacoco/index.html tika-core/target/site/jacoco/jacoco.csv tika-core/target/site/jacoco/jacoco.xml $OUT/jacoco-$1/; cp -r tika-core/target/site/jacoco/org.apache.tika.io tika-core/target/site/jacoco/jacoco-resources $OUT/jacoco-$1/; grep -E ",(EndianUtils|FilenameUtils)," tika-core/target/site/jacoco/jacoco.csv; }
pit 1-originaux "org.apache.tika.io.*_Test,org.apache.tika.io.*ManualTest"
pit 2-generation-passe1 "org.apache.tika.io.EndianUtils_getIntLE_20_0_Test,org.apache.tika.io.EndianUtils_getIntLE_21_0_Test,org.apache.tika.io.EndianUtils_getShortLE_12_0_Test,org.apache.tika.io.EndianUtils_getShortLE_13_0_Test,org.apache.tika.io.EndianUtils_getUIntBE_26_0_Test,org.apache.tika.io.EndianUtils_getUIntBE_27_0_Test,org.apache.tika.io.EndianUtils_getUShortBE_18_0_Test,org.apache.tika.io.EndianUtils_getUShortBE_19_0_Test,org.apache.tika.io.EndianUtils_getUShortLE_14_0_Test,org.apache.tika.io.EndianUtils_getUShortLE_15_0_Test,org.apache.tika.io.EndianUtils_readUE7_11_0_Test,org.apache.tika.io.FilenameUtils_resolveWithin_5_0_Test,org.apache.tika.io.EndianUtils_readShortLE_0_0_Test,org.apache.tika.io.EndianUtils_readShortBE_1_0_Test,org.apache.tika.io.EndianUtils_readUShortLE_2_0_Test,org.apache.tika.io.EndianUtils_readUIntLE_4_0_Test,org.apache.tika.io.EndianUtils_readUIntBE_5_0_Test,org.apache.tika.io.EndianUtils_readIntBE_7_0_Test,org.apache.tika.io.EndianUtils_readIntME_8_0_Test,org.apache.tika.io.FilenameUtils_getSanitizedEmbeddedFileName_3_0_Test,org.apache.tika.io.FilenameUtils_getSanitizedEmbeddedFilePath_4_0_Test,org.apache.tika.io.FilenameUtils_calculateExtension_10_0_Test,org.apache.tika.io.*ManualTest"
pit 3-generation-passe2 "org.apache.tika.io.*ManualTest"
pit 4-final ""
jac 1-originaux '!*_Test,!*ManualTest'
jac 2-generation-passe1 '!EndianUtils_getIntLE_20_0_Test,!EndianUtils_getIntLE_21_0_Test,!EndianUtils_getShortLE_12_0_Test,!EndianUtils_getShortLE_13_0_Test,!EndianUtils_getUIntBE_26_0_Test,!EndianUtils_getUIntBE_27_0_Test,!EndianUtils_getUShortBE_18_0_Test,!EndianUtils_getUShortBE_19_0_Test,!EndianUtils_getUShortLE_14_0_Test,!EndianUtils_getUShortLE_15_0_Test,!EndianUtils_readUE7_11_0_Test,!FilenameUtils_resolveWithin_5_0_Test,!EndianUtils_readShortLE_0_0_Test,!EndianUtils_readShortBE_1_0_Test,!EndianUtils_readUShortLE_2_0_Test,!EndianUtils_readUIntLE_4_0_Test,!EndianUtils_readUIntBE_5_0_Test,!EndianUtils_readIntBE_7_0_Test,!EndianUtils_readIntME_8_0_Test,!FilenameUtils_getSanitizedEmbeddedFileName_3_0_Test,!FilenameUtils_getSanitizedEmbeddedFilePath_4_0_Test,!FilenameUtils_calculateExtension_10_0_Test,!*ManualTest'
jac 3-generation-passe2 '!*ManualTest'
jac 4-final ''
echo FINI
