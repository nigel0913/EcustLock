LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)  

LOCAL_CPPFLAGS += -fexceptions
LOCAL_MODULE    := libalize
LOCAL_SRC_FILES := alizeString.cpp AudioFileReader.cpp AudioFrame.cpp AudioInputStream.cpp AutoDestructor.cpp BoolMatrix.cpp CmdLine.cpp ConfigChecker.cpp Config.cpp ConfigFileReaderAbstract.cpp ConfigFileReaderRaw.cpp ConfigFileReaderXml.cpp ConfigFileWriter.cpp Distrib.cpp DistribGD.cpp DistribGF.cpp DistribRefVector.cpp DoubleSquareMatrix.cpp Exception.cpp Feature.cpp FeatureFileList.cpp FeatureFileReaderAbstract.cpp FeatureFileReader.cpp FeatureFileReaderHTK.cpp FeatureFileReaderRaw.cpp FeatureFileReaderSingle.cpp FeatureFileReaderSPro3.cpp FeatureFileReaderSPro4.cpp FeatureFileWriter.cpp FeatureFlags.cpp FeatureInputStream.cpp FeatureInputStreamModifier.cpp FeatureMultipleFileReader.cpp FeatureServer.cpp FileReader.cpp FileWriter.cpp FrameAcc.cpp FrameAccGD.cpp FrameAccGF.cpp Histo.cpp Label.cpp LabelFileReader.cpp LabelServer.cpp LabelSet.cpp LKVector.cpp Matrix.cpp Mixture.cpp MixtureDict.cpp MixtureFileReaderAbstract.cpp MixtureFileReaderAmiral.cpp MixtureFileReader.cpp MixtureFileReaderRaw.cpp MixtureFileReaderXml.cpp MixtureFileWriter.cpp MixtureGD.cpp MixtureGDStat.cpp MixtureGF.cpp MixtureGFStat.cpp MixtureServer.cpp MixtureServerFileReaderAbstract.cpp MixtureServerFileReader.cpp MixtureServerFileReaderRaw.cpp MixtureServerFileReaderXml.cpp MixtureServerFileWriter.cpp MixtureStat.cpp Object.cpp SegAbstract.cpp SegCluster.cpp Seg.cpp SegServer.cpp SegServerFileReaderAbstract.cpp SegServerFileReaderRaw.cpp SegServerFileWriter.cpp StatServer.cpp ULongVector.cpp ViterbiAccum.cpp XLine.cpp XList.cpp XListFileReader.cpp XmlParser.cpp  
  
include $(BUILD_STATIC_LIBRARY) 

include $(CLEAR_VARS)

LOCAL_MODULE    := EcustLock
LOCAL_SRC_FILES := EcustLock.cpp
LOCAL_WHOLE_STATIC_LIBRARIES := libalize

include $(BUILD_SHARED_LIBRARY)
