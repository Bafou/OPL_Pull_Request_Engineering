//CHECKSTYLE.OFF: UnusedImports
import java.util.HashSet;
import java.util.Set;
    private final Set<String> visitedInsnAddrPairs;
    private final short[] insns1;
    private final short[] insns2;
        this.insns1 = insns1;
        this.insns2 = insns2;

        visitedInsnAddrPairs = new HashSet<>();
        this.visitedInsnAddrPairs.clear();

                final String addrPairStr = insnAddress1 + "-" + insnAddress2;
                if (this.visitedInsnAddrPairs.add(addrPairStr)) {
                    // If we haven't compared target insns, following the control flow
                    // and do further compare.
                    return isSameInstruction(insnHolder1.target, insnHolder2.target);
                } else {
                    // If we have already compared target insns, here we can return
                    // true directly.
                    return true;
                }
                Log.i(TAG, "verify dex file:" + file.getPath() + " md5, use time: " + (System.currentTimeMillis() - start));
        long start = System.currentTimeMillis();
            Log.i(TAG, "verify resource file:" + resourceFile.getPath() + " md5, use time: " + (System.currentTimeMillis() - start));
            Log.i(TAG, "monkeyPatchExistingResources resource file:" + resourceString + ", use time: " + (System.currentTimeMillis() - start));

    public static int checkPackageAndTinkerFlag(ShareSecurityCheck securityCheck, int tinkerFlag) {

        return ShareConstants.ERROR_PACKAGE_CHECK_OK;
    }

import java.util.ArrayList;
        generateUnsignedApk(unSignedApk);
            ArrayList<String> command = new ArrayList<>();
            command.add("jarsigner");
            command.add("-sigalg");
            command.add("MD5withRSA");
            command.add("-digestalg");
            command.add("SHA1");
            command.add("-keystore");
            command.add(config.mSignatureFile.getAbsolutePath());
            command.add("-storepass");
            command.add(config.mStorePass);
            command.add("-keypass");
            command.add(config.mKeyPass);
            command.add("-signedjar");
            command.add(output.getAbsolutePath());
            command.add(input.getAbsolutePath());
            command.add(config.mStoreAlias);

            Process process = new ProcessBuilder(command).start();
            process.waitFor();
            process.destroy();
                throw new IOException("Can't Generate signed APK. Please check if your sign info is correct.");
    private void generateUnsignedApk(File output) throws IOException {
        Logger.d("Generate unsigned apk: %s", output.getName());
import java.util.Comparator;
        writePatchOperations(buffer, this.stringDataSectionDiffAlg.getPatchOperationList());
        writePatchOperations(buffer, this.typeIdSectionDiffAlg.getPatchOperationList());
        writePatchOperations(buffer, this.typeListSectionDiffAlg.getPatchOperationList());
        writePatchOperations(buffer, this.protoIdSectionDiffAlg.getPatchOperationList());
        writePatchOperations(buffer, this.fieldIdSectionDiffAlg.getPatchOperationList());
        writePatchOperations(buffer, this.methodIdSectionDiffAlg.getPatchOperationList());
        writePatchOperations(buffer, this.annotationSectionDiffAlg.getPatchOperationList());
        writePatchOperations(buffer, this.annotationSetSectionDiffAlg.getPatchOperationList());
        writePatchOperations(buffer, this.annotationSetRefListSectionDiffAlg.getPatchOperationList());
        writePatchOperations(buffer, this.annotationsDirectorySectionDiffAlg.getPatchOperationList());
        writePatchOperations(buffer, this.debugInfoSectionDiffAlg.getPatchOperationList());
        writePatchOperations(buffer, this.codeSectionDiffAlg.getPatchOperationList());
        writePatchOperations(buffer, this.classDataSectionDiffAlg.getPatchOperationList());
        writePatchOperations(buffer, this.encodedArraySectionDiffAlg.getPatchOperationList());
        writePatchOperations(buffer, this.classDefSectionDiffAlg.getPatchOperationList());
    private <T extends Comparable<T>> void writePatchOperations(
            DexDataBuffer buffer, List<PatchOperation<T>> patchOperationList
    ) {
        List<Integer> delOpIndexList = new ArrayList<>(patchOperationList.size());
        List<Integer> addOpIndexList = new ArrayList<>(patchOperationList.size());
        List<Integer> replaceOpIndexList = new ArrayList<>(patchOperationList.size());
        List<T> newItemList = new ArrayList<>(patchOperationList.size());

        for (PatchOperation<T> patchOperation : patchOperationList) {
            switch (patchOperation.op) {
                case PatchOperation.OP_DEL: {
                    delOpIndexList.add(patchOperation.index);
                    break;
                }
                case PatchOperation.OP_ADD: {
                    addOpIndexList.add(patchOperation.index);
                    newItemList.add(patchOperation.newItem);
                    break;
                }
                case PatchOperation.OP_REPLACE: {
                    replaceOpIndexList.add(patchOperation.index);
                    newItemList.add(patchOperation.newItem);
                    break;
        }
        buffer.writeUleb128(delOpIndexList.size());
        int lastIndex = 0;
        for (Integer index : delOpIndexList) {
            buffer.writeSleb128(index - lastIndex);
            lastIndex = index;
        }
        buffer.writeUleb128(addOpIndexList.size());
        lastIndex = 0;
        for (Integer index : addOpIndexList) {
            buffer.writeSleb128(index - lastIndex);
            lastIndex = index;
        }
        buffer.writeUleb128(replaceOpIndexList.size());
        lastIndex = 0;
        for (Integer index : replaceOpIndexList) {
            buffer.writeSleb128(index - lastIndex);
            lastIndex = index;
        }
        for (T newItem : newItemList) {
            if (newItem instanceof StringData) {
                buffer.writeStringData((StringData) newItem);
            } else
            if (newItem instanceof Integer) {
                // TypeId item.
                buffer.writeInt((Integer) newItem);
            } else
            if (newItem instanceof TypeList) {
                buffer.writeTypeList((TypeList) newItem);
            } else
            if (newItem instanceof ProtoId) {
                buffer.writeProtoId((ProtoId) newItem);
            } else
            if (newItem instanceof FieldId) {
                buffer.writeFieldId((FieldId) newItem);
            } else
            if (newItem instanceof MethodId) {
                buffer.writeMethodId((MethodId) newItem);
            } else
            if (newItem instanceof Annotation) {
                buffer.writeAnnotation((Annotation) newItem);
            } else
            if (newItem instanceof AnnotationSet) {
                buffer.writeAnnotationSet((AnnotationSet) newItem);
            } else
            if (newItem instanceof AnnotationSetRefList) {
                buffer.writeAnnotationSetRefList(
                        (AnnotationSetRefList) newItem
                );
            } else
            if (newItem instanceof AnnotationsDirectory) {
                buffer.writeAnnotationsDirectory(
                        (AnnotationsDirectory) newItem
                );
            } else
            if (newItem instanceof DebugInfoItem) {
                buffer.writeDebugInfoItem((DebugInfoItem) newItem);
            } else
            if (newItem instanceof Code) {
                buffer.writeCode((Code) newItem);
            } else
            if (newItem instanceof ClassData) {
                buffer.writeClassData((ClassData) newItem);
            } else
            if (newItem instanceof EncodedValue) {
                buffer.writeEncodedArray((EncodedValue) newItem);
            } else
            if (newItem instanceof ClassDef) {
                buffer.writeClassDef((ClassDef) newItem);
            } else {
                throw new IllegalStateException(
                        "Unknown item type: " + newItem.getClass()
                );
     *
     * Here we mark deleted item for such a case like this:
     *   Item in DebugInfo section reference a string in StringData section
     *   by index X, while in patched dex, the referenced string is removed.
     *
     * The {@code indexMap} must be aware of this case and return -1
     * instead of the original value X.
     *
     * Further more, the special value -1 is not chosen by our inspiration but
     * the definition of NO_INDEX in document of dex file format.
                return Float.compare(oldFloat, newFloat) == 0;
                return Double.compare(oldDouble, newDouble) == 0;
     *
     * Here we mark deleted item for such a case like this:
     *   Item in DebugInfo section reference a string in StringData section
     *   by index X, while in patched dex, the referenced string is removed.
     *
     * The {@code indexMap} must be aware of this case and return -1
     * instead of the original value X.
     *
     * Further more, the special value -1 is not chosen by our inspiration but
     * the definition of NO_INDEX in document of dex file format.
                markDeletedIndexOrOffset(
                        oldToFullPatchedIndexMap,
                        oldIndex,
                        getItemOffsetOrIndex(oldIndex, skippedOldItem)
                );
                markDeletedIndexOrOffset(
                        oldToFullPatchedIndexMap,
                        oldIndex,
                        getItemOffsetOrIndex(oldIndex, skippedOldItem)
                );
                markDeletedIndexOrOffset(
                        oldToFullPatchedIndexMap,
                        oldIndex,
                        getItemOffsetOrIndex(oldIndex, skippedOldItem)
                );
                markDeletedIndexOrOffset(
                        oldToFullPatchedIndexMap,
                        oldIndex,
                        getItemOffsetOrIndex(oldIndex, skippedOldItem)
                );
//CHECKSTYLE.ON: UnusedImports