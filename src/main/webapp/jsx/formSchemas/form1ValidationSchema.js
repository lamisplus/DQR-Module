import { useFormik } from "formik";
import * as yup from "yup";

export const useValidateForm1ValuesHook = (onSubmit) => {
  const form1InitialValues = {
    hospitalNumber: "",
    surname: "",
    otherName: "",
    phone: "",
    residentialAddress:"",
    landmark: "",
    country: "",
    state: "",
    lga: "",
    dateOfBirth: "",
    age: "",
    occupation: "",
    maritalStatus: "",
    education: "",
    careEntryPoint: "",
    weight: "",
    height: "",
    bmi: "",
    pregnancy: "",
    breastfeeding: "",
    historyOfUsingAbusedSubstance: "",
    hbsAg: "",
    dateOfHepatitisBPositiveScreening: "",
    hcvAb: ""
  };

  const Form1ValidationSchema = yup.object({
    hospitalNumber: yup.string(),
    surname: yup.string(),
    otherName: yup.string(),
    phone: yup.string(),
    residentialAddress:yup.string(),
    landmark: yup.string(),
    country: yup.string(),
    state: yup.string(),
    lga: yup.string(),
    dateOfBirth: yup.string(),
    age: yup.string(),
    occupation: yup.string(),
    maritalStatus: yup.string(),
    education: yup.string(),
    careEntryPoint: yup.string(),
    weight: yup.string(),
    height: yup.string(),
    bmi: yup.string(),
    pregnancy: yup.string(),
    breastfeeding: yup.string(),
    historyOfUsingAbusedSubstance: yup.string(),
    hbsAg: yup.string(),
    dateOfHepatitisBPositiveScreening: yup.string(),
    hcvAb: yup.string()
  });

  const formik = useFormik({
    initialValues: form1InitialValues,
    onSubmit,
    validationSchema: Form1ValidationSchema,
  });
   return { formik };;
};


export const useValidateForm2ValuesHook = (onSubmit) => {
  const form2InitialValues = {
    dateHbvTestRequested: "",
    dateHbvSampleCollected: "",
    dateHbvDnaResultReported: "",
    hbvDna: "",
    hbvDnaValue: "",
    hbeAg: "",
    hbsAgQuantification: "",
    antiHdv: "",
    treatmentEligible: "",
    pmtctEligible: "",
    comment: "",
    hcvRna: "",
    hcvValue: "",
    hepatitisCoInfection: [],
    specifyMulitipleInfection: "",
    alt: "",
    ast: "",
    plt: "",
    altValue: "",
    astValue: "",
    pltValue: "",
    totalBilirubin: "",
    directBilirubin: "",
    albumin: "",
    apriScore: "",
    fib4: "",
    prothrombinTime: "",
    urea: "",
    creatinine: "",
    ultrasoundScan: "",
    afp: "",
    fibroscan: "",
    ctScan: "",
    ascites: "",
    severityOfAscites: "",
    gradeOfEncephalopathy: "",
    childPughScore: "",
    liverBiopsyStage: "",
    stagingDateForLiverBiopsy: "",
    diagnosis: "",
    commobidities: ""
    
  };

  const Form2ValidationSchema = yup.object({
    dateHbvTestRequested:yup.string(),
    dateHbvSampleCollected:yup.string(),
    dateHbvDnaResultReported:yup.string(),
    hbvDna:yup.string(),
    hbvDnaValue:yup.string(),
    hbeAg:yup.string(),
    hbsAgQuantification:yup.string(),
    antiHdv:yup.string(),
    treatmentEligible:yup.string(),
    pmtctEligible:yup.string(),
    comment:yup.string(),
    altValue: yup.string(),
    astValue: yup.string(),
    pltValue: yup.string(),
    hcvRna:yup.string(),
    hcvValue:yup.string(),
    hepatitisCoInfection:yup.array(),
    specifyMulitipleInfection:yup.string(),
    alt:yup.string(),
    ast:yup.string(),
    plt:yup.string(),
    commobidities:yup.string(),
    totalBilirubin:yup.string(),
    directBilirubin:yup.string(),
    albumin:yup.string(),
    apriScore:yup.string(),
    fib4:yup.string(),
    prothrombinTime:yup.string(),
    urea:yup.string(),
    creatinine:yup.string(),
    ultrasoundScan:yup.string(),
    afp:yup.string(),
    fibroscan:yup.string(),
    ctScan:yup.string(),
    ascites:yup.string(),
    severityOfAscites:yup.string(),
    gradeOfEncephalopathy:yup.string(),
    childPughScore:yup.string(),
    liverBiopsyStage:yup.string(),
    stagingDateForLiverBiopsy:yup.string(),
    diagnosis:yup.string()
  });

  const formik = useFormik({
    initialValues: form2InitialValues,
    onSubmit,
    validationSchema: Form2ValidationSchema,
  });
   return { formik };;
};


export const useValidateForm3ValuesHook = (onSubmit) => {
  const form3InitialValues = {
   hbvTreatmentExperience: "",
   hbvPastTreatmentRegimen: "",
   hbvNewRegimen: "",
   hbvDateStarted: "",
   hbvHistoryOfAdverseEffect: "",

   hbvRegimeSwitchNewRegimen: "",
   hbvRegimeSwitchDateStarted: "",
   hbvRegimeSwitchHistoryOfAdverseEffect: "",
   hbvRegimeSwitchReason: "",
   hbvRegimeSwitchDateStopped: "",
   hbvReasonForTreatmentEligibility: "",
   hbvReasonsForTreatmentComment: "",

   hcvTreatmentExperience: "",
   hcvPastTreatmentExperience: "",
   hcvDateStarted: "",
   hcvDateCompleted: "",
   hcvPrescribedDuration: "",
   hcvAdverseEventReported: "",

   hcvNewRegimen: "",
   hcvRegimeSwitchNewRegimen: "",
   hcvRegimeSwitchPrescribedDuration: "",
   hcvRegimeSwitchDateStarted: "",
   hcvRegimeSwitchDateStopped: "",
   hcvRegimeSwitchHistoryOfAdverseEffect: "",
  
   svr12TestingDateStarted: "",
   svr12TestingHcvRna: "",
   svr12TestingHcvRnaValue: "",
   svr12RetreatmentDateTested: "",
   svr12RetreatmentHcvRna: "",
   svr12RetreatmentHcvRnaValue: "",

   hcvRetreatmentNewRegime: "",
   hcvRetreatmentPrescribedDuration: "",
   hcvRetreatmentDateStarted: "",
   hcvRetreatmentDateStopped: "",
   hcvRetreatmentAdverseEffect: "",
   hcvRetreatmentHcvGenotype: "",
  };

  const Form3ValidationSchema = yup.object({
    hbvTreatmentExperience: yup.string(),
   hbvPastTreatmentRegimen: yup.string(),
   hbvNewRegimen: yup.string(),
   hbvDateStarted: yup.string(),
   hbvHistoryOfAdverseEffect: yup.string(),
   hbvRegimeSwitchNewRegimen: yup.string(),
   hbvRegimeSwitchDateStarted: yup.string(),
   hbvRegimeSwitchHistoryOfAdverseEffect: yup.string(),
   hbvRegimeSwitchReason: yup.string(),
   hbvRegimeSwitchDateStopped: yup.string(),
   hbvReasonForTreatmentEligibility: yup.string(),
   hbvReasonsForTreatmentComment: yup.string(),

   hcvTreatmentExperience: yup.string(),
   hcvPastTreatmentExperience: yup.string(),
   hcvDateStarted: yup.string(),
   hcvDateCompleted: yup.string(),
   hcvPrescribedDuration: yup.string(),
   hcvNewRegimen: yup.string(),
   hcvRegimeSwitchNewRegimen: yup.string(),
   hcvRegimeSwitchPrescribedDuration: yup.string(),
   hcvRegimeSwitchDateStarted: yup.string(),
   hcvRegimeSwitchDateStopped: yup.string(),
   hcvAdverseEventReported: yup.string(),
  
   svr12TestingDateStarted: yup.string(),
   svr12TestingHcvRna: yup.string(),
   svr12TestingHcvRnaValue: yup.string(),
   svr12RetreatmentDateTested: yup.string(),
   svr12RetreatmentHcvRna: yup.string(),
   svr12RetreatmentHcvRnaValue: yup.string(),

   hcvRetreatmentNewRegime: yup.string(),
   hcvRetreatmentPrescribedDuration: yup.string(),
   hcvRetreatmentDateStarted: yup.string(),
   hcvRetreatmentDateStopped: yup.string(),
   hcvRetreatmentAdverseEffect: yup.string(),
   hcvRetreatmentHcvGenotype: yup.string(),


  });

  const formik = useFormik({
    initialValues: form3InitialValues,
    onSubmit,
    validationSchema: Form3ValidationSchema,
  });
   return { formik };
};

export const useValidateFollowupFormValuesHook = (onSubmit) => {
  const followupFormIntialValue = {
    dateOfVisit: "",
    weight: "",
    height: "",
    bmi: "",
    bloodPressure: "",
    hbsAgQuantification: "",
    hbeAg: "",
    hbvDna: "",

    alt: "",
    ast: "",
    plt: "",
    totalBilirubin: "",
    directBilirubin: "",
    albumin: "",
    apriScore: "",
    fib4: "",
    prothrombinTime: "",
    urea: "",
    creatinine: "",
    ultrasoundScan: "",
    afp: "",
    fibroscan: "",
    ctScan: "",
    ascites: "",
    severityOfAscites: "",
    gradeOfEncephalopathy: "",
    childPughScore: "",
    liverBiopsyStage: "",
    stagingDateForLiverBiopsy: "",
    diagnosis: "",
    treatmentRegimen: "",
    nextAppointment: "",
    remark: "",

  };

  const FollowupFormValidationSchema = yup.object({
    dateOfVisit: yup.string(),
    weight: yup.string(),
    height: yup.string(),
    bmi: yup.string(),
    bloodPressure: yup.string(),
    hbsAgQuantification: yup.string(),
    hbeAg: yup.string(),
    hbvDna: yup.string(),

    alt: yup.string(),
    ast: yup.string(),
    plt: yup.string(),
    totalBilirubin: yup.string(),
    directBilirubin: yup.string(),
    albumin: yup.string(),
    apriScore: yup.string(),
    fib4: yup.string(),
    prothrombinTime: yup.string(),
    urea: yup.string(),
    creatinine: yup.string(),
    ultrasoundScan: yup.string(),
    afp: yup.string(),
    fibroscan: yup.string(),
    ctScan: yup.string(),
    ascites: yup.string(),
    severityOfAscites: yup.string(),
    gradeOfEncephalopathy: yup.string(),
    childPughScore: yup.string(),
    liverBiopsyStage: yup.string(),
    stagingDateForLiverBiopsy: yup.string(),
    diagnosis: yup.string()

  });

  const formik = useFormik({
    initialValues: followupFormIntialValue,
    onSubmit,
    validationSchema: FollowupFormValidationSchema,
  });
   return { formik };;
};



