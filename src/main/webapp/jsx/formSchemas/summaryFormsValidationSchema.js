import { useFormik } from "formik";
import * as yup from "yup";

export const useValidateSummaryValuesHook = (onSubmit) => {
  const reactiveFormInitialValues = {
   "maleNineAndUnder": "",
   "maleTen2Fourteen": "",
   "maleFifteenAndAbove": "",
   "maleTotal" : "",
   "femaleNineAndUnder": "",
   "femaleTen2Fourteen": "",
   "femaleFifteenAndAbove": "",
   "femaleTotal" : "",
   "specialPopulationPWID" : "",
   "specialPopulationPW" : "",
  };

  const ReactiveFormSchema = yup.object({
    "maleNineAndUnder": yup.string(),
    "maleTen2Fourteen": yup.string(),
    "maleFifteenAndAbove": yup.string(),
    "maleTotal" : yup.string(),
    "femaleNineAndUnder": yup.string(),
    "femaleTen2Fourteen": yup.string(),
    "femaleFifteenAndAbove": yup.string(),
    "femaleTotal" : yup.string(),
    "specialPopulationPWID" : yup.string(),
    "specialPopulationPW" : yup.string(),
  });

  const formik = useFormik({
    initialValues: reactiveFormInitialValues,
    onSubmit,
    validationSchema: ReactiveFormSchema,
  });
   return { formik };;
};