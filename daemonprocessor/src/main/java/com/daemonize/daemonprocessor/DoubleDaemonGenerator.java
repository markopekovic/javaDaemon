package com.daemonize.daemonprocessor;

import com.daemonize.daemonprocessor.annotations.CallingThread;
import com.daemonize.daemonprocessor.annotations.Daemonize;
import com.daemonize.daemonprocessor.annotations.SideQuest;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

public class DoubleDaemonGenerator extends BaseDaemonGenerator {

    protected MainQuestDaemonGenerator mainGenerator;
    protected SideQuestDaemonGenerator sideGenerator;

    private final String MAIN_DAEMON_ENGINE_STRING = "mainDaemonEngine";
    private final String SIDE_DAEMON_ENGINE_STRING = "sideDaemonEngine";

    public DoubleDaemonGenerator(TypeElement classElement) {
        super(classElement);
        this.mainGenerator = new MainQuestDaemonGenerator(
                classElement,
                true,
                classElement.getAnnotation(Daemonize.class).returnDaemonInstance()
        );
        this.sideGenerator = new SideQuestDaemonGenerator(classElement);

        mainGenerator.setDaemonEngineString(MAIN_DAEMON_ENGINE_STRING).setAutoGenerateApiMethods(false);
        sideGenerator.setDaemonEngineString(SIDE_DAEMON_ENGINE_STRING).setAutoGenerateApiMethods(false);
    }


    @Override
    public TypeSpec generateDaemon(List<ExecutableElement> publicPrototypeMethods) {

        TypeSpec.Builder daemonClassBuilder = TypeSpec.classBuilder(daemonSimpleName)
                .addModifiers(
                        Modifier.PUBLIC
                ).addSuperinterface(daemonInterface);

        daemonClassBuilder = addTypeParameters(classElement, daemonClassBuilder);

        //private field for prototype
        FieldSpec prototype = FieldSpec.builder(
                ClassName.get(classElement.asType()),
                PROTOTYPE_STRING
        ).addModifiers(Modifier.PRIVATE).build();

        //main quest daemon engine
        ClassName mainDaemonEngineClass = ClassName.get(
                mainGenerator.getDaemonPackage(),
                mainGenerator.getDaemonEngineSimpleName()
        );

        FieldSpec mainDaemonEngine = FieldSpec.builder(
                mainDaemonEngineClass,
                MAIN_DAEMON_ENGINE_STRING
        )
        .addModifiers(Modifier.PROTECTED)
        .build();

        //side quest daemon engine
        ClassName sideDaemonEngineClass = ClassName.get(
                sideGenerator.getDaemonPackage(),
                sideGenerator.getDaemonEngineSimpleName()
        );

        FieldSpec sideDaemonEngine = FieldSpec.builder(
                sideDaemonEngineClass,
                SIDE_DAEMON_ENGINE_STRING
        )
        .addModifiers(Modifier.PROTECTED)
        .build();

        daemonClassBuilder.addField(prototype);
        daemonClassBuilder.addField(mainDaemonEngine);
        daemonClassBuilder.addField(sideDaemonEngine);

        //daemon construct
        MethodSpec.Builder daemonConstructorBuilder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(consumer, "mainConsumer")
                .addParameter(consumer, "sideConsumer")
                .addParameter(ClassName.get(classElement.asType()), PROTOTYPE_STRING)
                .addStatement("this.$N = new $N(mainConsumer).setName(this.getClass().getSimpleName() + \" - MAIN\")", MAIN_DAEMON_ENGINE_STRING, mainGenerator.getDaemonEngineSimpleName())
                .addStatement("this.$N = new $N(sideConsumer).setName(this.getClass().getSimpleName() + \" - SIDE\")", SIDE_DAEMON_ENGINE_STRING, sideGenerator.getDaemonEngineSimpleName());

        //add dedicated daemon engines
        for (Map.Entry<ExecutableElement, Pair<String, FieldSpec>> entry : mainGenerator.getDedicatedThreadEngines().entrySet()) {
            daemonClassBuilder.addField(entry.getValue().getSecond());
            daemonConstructorBuilder.addStatement(
                    "this." + entry.getValue().getFirst() +
                            " = new $N(mainConsumer).setName(this.getClass().getSimpleName() + \" - "
                            + entry.getValue().getFirst() + "\")",
                    mainGenerator.getDaemonEngineSimpleName()
//                    daemonEngineSimpleName
            );
        }

        MethodSpec daemonConstructor = daemonConstructorBuilder
                .addStatement("this.$N = $N", PROTOTYPE_STRING, PROTOTYPE_STRING)
                .build();

        daemonClassBuilder.addMethod(daemonConstructor);

        //sideQuest daemon fields and methods
        List<Pair<ExecutableElement, SideQuest>> sideQuests
                = getSideQuestMethods(publicPrototypeMethods);

        List<Pair<TypeSpec, MethodSpec>> sideQuestFields = new ArrayList<>();

        for (Pair<ExecutableElement, SideQuest> sideQuestPair : sideQuests) {
            sideQuestFields.add(sideGenerator.createSideQuest(sideQuestPair));
        }

        //add side quest setters
        for (Pair<TypeSpec, MethodSpec> sideQuestField : sideQuestFields) {
            daemonClassBuilder.addMethod(sideQuestField.getSecond());
        }

        Map<TypeSpec, MethodSpec> mainQuestsAndApiMethods = new LinkedHashMap<>();

        for (ExecutableElement method : publicPrototypeMethods) {

            if (method.getAnnotation(CallingThread.class) != null) {
                daemonClassBuilder.addMethod(mainGenerator.copyMethod(method));
                continue;
            }

            if (mainGenerator.getDedicatedThreadEngines().containsKey(method)) {
                mainQuestsAndApiMethods.put(
                        mainGenerator.createMainQuest(method),
                        mainGenerator.createApiMethod(
                                method,
                                mainGenerator.getDedicatedThreadEngines().get(method).getFirst()
                        )
                );
            } else {
                mainQuestsAndApiMethods.put(
                        mainGenerator.createMainQuest(method),
                        mainGenerator.createApiMethod(method, mainGenerator.getDaemonEngineString())
                );
            }
        }

        //add side quests
        for (Pair<TypeSpec, MethodSpec> sideQuestField : sideQuestFields) {
            daemonClassBuilder.addType(sideQuestField.getFirst());
        }

        //add main quest methods
        for (Map.Entry<TypeSpec, MethodSpec> entry : mainQuestsAndApiMethods.entrySet()) {
            daemonClassBuilder.addMethod(entry.getValue());
        }

        //Add API METHODS
        List<MethodSpec> apiMethods = new ArrayList<>(7);


        apiMethods.add(generateGetPrototypeDaemonApiMethod());
        apiMethods.add(generateSetPrototypeDaemonApiMethod());
        apiMethods.add(sideGenerator.generateStartDaemonApiMethod());
        apiMethods.add(generateStopDaemonApiMethod());
        apiMethods.add(generateQueueStopDaemonApiMethod());//TODO override !!!!!!!!!!!!!!!!!!!!!!!!!!
        apiMethods.add(mainGenerator.generateGetStateDaemonApiMethod());
        apiMethods.add(generateSetNameDaemonApiMethod());
        apiMethods.add(mainGenerator.generateGetNameDaemonApiMethod());//TODO CHECK THISSS!!!!!!!
        apiMethods.add(generateSetMainConsumerDaemonApiMethod());
        apiMethods.add(generateSetSideConsumerDaemonApiMethod());
        apiMethods.add(generateSetConsumerDaemonApiMethod());

        for(MethodSpec apiMethod : apiMethods) {
            daemonClassBuilder.addMethod(apiMethod);
        }

        //add main quests
        for (Map.Entry<TypeSpec, MethodSpec> entry : mainQuestsAndApiMethods.entrySet()) {
            daemonClassBuilder.addType(entry.getKey());
        }

        return daemonClassBuilder.build();

    }

    @Override
    public MethodSpec generateStopDaemonApiMethod() {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("stop")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class)
                .addStatement(mainGenerator.getDaemonEngineString() + ".stop()")
                .addStatement(sideGenerator.getDaemonEngineString() + ".stop()");

        for (Map.Entry<ExecutableElement, Pair<String, FieldSpec>> entry : mainGenerator.getDedicatedThreadEngines().entrySet()) {
            builder.addStatement( entry.getValue().getFirst() + ".stop()");

        }

        return builder.build();
    }

    @Override
    public MethodSpec generateQueueStopDaemonApiMethod() {

        MethodSpec.Builder builder = MethodSpec.methodBuilder("queueStop")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class)
                .addStatement(mainGenerator.getDaemonEngineString() + ".queueStop()")
                .addStatement(sideGenerator.getDaemonEngineString() + ".stop()");

        for (Map.Entry<ExecutableElement, Pair<String, FieldSpec>> entry : mainGenerator.getDedicatedThreadEngines().entrySet()) {
            builder.addStatement( entry.getValue().getFirst() + ".queueStop()");
        }

        return builder.build();
    }

    @Override
    public MethodSpec generateSetNameDaemonApiMethod() {

        MethodSpec.Builder builder =  MethodSpec.methodBuilder("setName")
                .addParameter(String.class, "name")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(ClassName.get(packageName, daemonSimpleName))
                .addStatement(mainGenerator.getDaemonEngineString() + ".setName(name + \" - MAIN\")")
                .addStatement(sideGenerator.getDaemonEngineString() + ".setName(name + \" - SIDE\")");

        for (Map.Entry<ExecutableElement, Pair<String, FieldSpec>> entry : mainGenerator.getDedicatedThreadEngines().entrySet()) {
            builder.addStatement(entry.getValue().getFirst() + ".setName(name +\" - " + entry.getValue().getFirst() + "\")");
        }

        return builder.addStatement("return this").build();
    }

    public MethodSpec generateSetMainConsumerDaemonApiMethod() {
        return MethodSpec.methodBuilder("setMainQuestConsumer")
                .addParameter(consumer, "consumer")
                .addModifiers(Modifier.PUBLIC)
                .returns(ClassName.get(packageName, daemonSimpleName))
                .addStatement(mainGenerator.getDaemonEngineString() + ".setConsumer(consumer)")
                .addStatement("return this")
                .build();
    }

    public MethodSpec generateSetSideConsumerDaemonApiMethod() {
        return MethodSpec.methodBuilder("setSideQuestConsumer")
                .addParameter(consumer, "consumer")
                .addModifiers(Modifier.PUBLIC)
                .returns(ClassName.get(packageName, daemonSimpleName))
                .addStatement(sideGenerator.getDaemonEngineString() + ".setConsumer(consumer)")
                .addStatement("return this")
                .build();
    }

    @Override
    public MethodSpec generateSetConsumerDaemonApiMethod() { //TODO check whether to throw or set consumer to both engines
        return MethodSpec.methodBuilder("setConsumer")
                .addParameter(consumer, "consumer")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(ClassName.get(packageName, daemonSimpleName))
                .addStatement("throw new $T(\"This method is unusable in DoubleDaemon. Please use setMainQuestConsumer(Consumer consumer) or setSideQuestConsumer(Consumer consumer)\")", IllegalStateException.class)
                .build();
    }
}