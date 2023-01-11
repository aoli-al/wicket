#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import os
import subprocess
import sys
import click
DIR = os.path.dirname(os.path.realpath(__file__))
sys.path.append(os.path.join(DIR, ".."))
from commons import *

JAR_NAME = "wicket-core-10.0.0-M1-SNAPSHOT-test-jar-with-dependencies.jar"
ORIGIN_JAR_PATH = "wicket-core/target"
TEST_CLASS = "org.apache.wicket.model.LoadableDetachableModelTest"
APPLICATION_NAMESPACE = "Lorg/apache/wicket"
@click.group(name="mode")
def main():
    pass


@main.command(name="build")
def build():
    subprocess.call(["mvn", "install", "-DskipTests"], cwd=DIR)


@main.command(name="instrument")
def instrument():
    subprocess.call(["java",
                     f"-DPhosphor.INSTRUMENTATION_CLASSPATH={INSTRUMENTATION_CLASSPATH}",
                     f"-DPhosphor.ORIGIN_CLASSPATH={ORIGIN_CLASSPATH}",
                     "-cp", PHOSPHOR_JAR_PATH, "edu.columbia.cs.psl.phosphor.Instrumenter",
                     f"{ORIGIN_JAR_PATH}/{JAR_NAME}", INSTRUMENTATION_FOLDER_NAME,
                     "-taintTagFactory", "al.aoli.exchain.phosphor.instrumenter.DynamicSwitchTaintTagFactory"], cwd=DIR)
    subprocess.call(["java",
                     f"-DPhosphor.INSTRUMENTATION_CLASSPATH={HYBRID_CLASSPATH}",
                     "-cp", PHOSPHOR_JAR_PATH, "edu.columbia.cs.psl.phosphor.Instrumenter",
                     f"{ORIGIN_JAR_PATH}/{JAR_NAME}", HYBRID_FOLDER_NAME,
                     "-taintTagFactory", "al.aoli.exchain.phosphor.instrumenter.FieldOnlyTaintTagFactory",
                     "-postClassVisitor", "al.aoli.exchain.phosphor.instrumenter.UninstrumentedOriginPostCV"
                     ], cwd=DIR)


@main.command(name="origin")
@click.option('--debug', default=False, help='Enable debugging.')
def origin(debug: bool):
    command = ["-cp", f"{ORIGIN_JAR_PATH}/{JAR_NAME}", TEST_CLASS]
    if debug:
        command.insert(0, "-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005")
    subprocess.call(["java"] + command)

@main.command(name="static")
def static():
    subprocess.call(["java",
                     "-cp",
                     f"{ORIGIN_JAR_PATH}/{JAR_NAME}",
                     f"-javaagent:{RUNTIME_JAR_PATH}=static:{INSTRUMENTATION_CLASSPATH}",
                     f"-agentpath:{NATIVE_LIB_PATH}=exchain:L{APPLICATION_NAMESPACE}",
                     TEST_CLASS])
    args = ["./gradlew", "static-analyzer:run", f"--args={ORIGIN_CLASSPATH} {DIR}/static-results {ORIGIN_CLASSPATH}"]
    print(args)
    subprocess.call(args, cwd=os.path.join(DIR, "../.."))


@main.command(name="hybrid")
@click.option('--debug', is_flag=True, default=False, help='Enable debugging.')
def hybrid(debug: bool):
    cmd = [HYBRID_JAVA_EXEC,
            "-DPhosphor.DEBUG=true",
           f"-DPhosphor.INSTRUMENTATION_CLASSPATH={HYBRID_CLASSPATH}",
           f"-DPhosphor.ORIGIN_CLASSPATH={ORIGIN_CLASSPATH}",
           "-cp", f"{HYBRID_FOLDER_NAME}/{JAR_NAME}",
           f"-javaagent:{PHOSPHOR_AGENT_PATH}=taintTagFactory=al.aoli.exchain.phosphor.instrumenter.FieldOnlyTaintTagFactory,postClassVisitor=al.aoli.exchain.phosphor.instrumenter.UninstrumentedOriginPostCV",
           f"-javaagent:{RUNTIME_JAR_PATH}=hybrid:{HYBRID_CLASSPATH}",
           f"-agentpath:{NATIVE_LIB_PATH}=exchain:Lorg/apache/hadoop/fs",
           TEST_CLASS]
    if debug:
        cmd.insert(
            1, "-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005")
    subprocess.call(cmd)

@main.command(name="dynamic")
@click.option('--debug', is_flag=True, default=False, help='Enable debugging.')
def dynamic(debug: bool):
    cmd = [INSTRUMENTED_JAVA_EXEC,
                     f"-DPhosphor.INSTRUMENTATION_CLASSPATH={INSTRUMENTATION_CLASSPATH}",
                     f"-DPhosphor.ORIGIN_CLASSPATH={ORIGIN_CLASSPATH}",
                     "-cp", f"{INSTRUMENTATION_FOLDER_NAME}/{JAR_NAME}",
                     f"-javaagent:{PHOSPHOR_AGENT_PATH}",
                     f"-javaagent:{RUNTIME_JAR_PATH}=dynamic:{INSTRUMENTATION_CLASSPATH}",
                     f"-agentpath:{NATIVE_LIB_PATH}=exchain:{APPLICATION_NAMESPACE}",
                     TEST_CLASS]
    if debug:
        cmd.insert(1, "-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005")
    subprocess.call(cmd)


if __name__ == '__main__':
    main()
