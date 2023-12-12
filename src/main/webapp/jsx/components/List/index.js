import React, { useState, useEffect} from "react";

import {Form } from "reactstrap";
import {makeStyles} from "@material-ui/core/styles";
import {Card, CardContent} from "@material-ui/core";

import {ToastContainer, toast} from "react-toastify";
import "react-toastify/dist/ReactToastify.css";
import "react-widgets/dist/css/react-widgets.css";
import {FaPlus, FaAngleDown} from 'react-icons/fa';
import Demographic  from './../DataCompleteness/Demographics/index'
import Clinical  from './../DataCompleteness/Clinical/index'
import Pharmacy  from './../DataConsistency/Pharmacy/index'
import Clinicals  from './../DataConsistency/Clinical/index'
import Biometric  from './../DataConsistency/Biometric/index'
import PrEP from "../DataConsistency/PrEp";
import Hts from "../DataConsistency/Hts";
import TbScreening from "../DataConsistency/TbScreening";
import Laboratory from "../DataConsistency/Laboratory";
import ValidityReport from "../DataValidity/ValidityReport";
import EAC from "../DataConsistency/Eac";




const useStyles = makeStyles((theme) => ({
    card: {
        margin: theme.spacing(20),
        display: "flex",
        flexDirection: "column",
        alignItems: "center"
    },
    form: {
        width: "100%", // Fix IE 11 issue.
        marginTop: theme.spacing(3),
    },
    submit: {
        margin: theme.spacing(3, 0, 2),
    },
    cardBottom: {
        marginBottom: 20,
    },
    Select: {
        height: 45,
        width: 300,
    },
    button: {
        margin: theme.spacing(1),
    },
    root: {
        '& > *': {
            margin: theme.spacing(1)
        },
        "& .card-title":{
            color:'#fff',
            fontWeight:'bold'
        },
        "& .form-control":{
            borderRadius:'0.25rem',
            height:'41px'
        },
        "& .card-header:first-child": {
            borderRadius: "calc(0.25rem - 1px) calc(0.25rem - 1px) 0 0"
        },
        "& .dropdown-toggle::after": {
            display: " block !important"
        },
        "& select":{
            "-webkit-appearance": "listbox !important"
        },
        "& p":{
            color:'red'
        },
        "& label":{
            fontSize:'14px',
            color:'#014d88',
            fontWeight:'bold'
        }
    },
    demo: {
        backgroundColor: theme.palette.background.default,
    },
    inline: {
        display: "inline",
    },
    error:{
        color: '#f85032',
        fontSize: '12.8px'
    },
    success: {
        color: "#4BB543 ",
        fontSize: "11px",
    },
}));


const DQAList = (props) => {
    const classes = useStyles();
    const [showDemographics, setShowDemographic] = useState(false);
    const [showClinical, setShowClinical] = useState(false);
    const [showClinicals, setShowClinicals] = useState(false);
    const [showPharmacy, setShowPharmacy] = useState(false);
    const [showBiometric, setShowBiometric] = useState(false);
    const [showPrep, setShowPrep] = useState(false);
    const [showHts, setShowHts] = useState(false);
    const [showNutrition, setShowNutrition] = useState(false);
    const [showGenderBase, setShowGenderBase] = useState(false);
    const [showChronicCondition, setShowChronicCondition] = useState(false);
    const [showTbScreening, setShowTbScreening] = useState(false);
    const [showReproductive, setShowReproductive] = useState(false);
    const [showEac, setShowEac] = useState(false);//Tpt
    const [showTpt, setShowTpt] = useState(false);
    const [showLaboratory, setShowLaboratory] = useState(false);
    const [showValidityReport, setShowValidityReport] = useState(false);

    const onClickDemographics =() =>{
        setShowDemographic(!showDemographics)
    }
    const onClickLaboratory =() =>{
        setShowLaboratory(!showLaboratory)
    }
    const onClickValidityReport =() =>{
        setShowValidityReport(!showValidityReport)
    }
    const onClickClinical =() =>{
        setShowClinical(!showClinical)
    }
    const onClickPharmacy =() =>{
            setShowPharmacy(!showPharmacy)
        }

    const onClickClinicals =() =>{
        setShowClinicals(!showClinicals)
    }
    const onClickPrep =() =>{
        setShowPrep(!showPrep)
    }
    const onClickHts =() =>{
        setShowHts(!showNutrition)
    }
    const onClickNutrition =() =>{
        setShowNutrition(!showNutrition)
    }
    const onClickBiometric =() =>{
            setShowBiometric(!showBiometric)
    }
    const onClickGenderBase =() =>{
        setShowGenderBase(!showGenderBase)
    }
    const onClickChronicCondition =() =>{
        setShowChronicCondition(!showChronicCondition)
    }
    const onClickTbScreening =() =>{
        setShowTbScreening(!showTbScreening)
    }
    const onClickReproductive =() =>{
        setShowReproductive(!showReproductive)
    }
    const onClickEac =() =>{
        setShowEac(!showEac)
    }

    return (
        <>
            <ToastContainer autoClose={3000} hideProgressBar />
            <div className="row page-titles mx-0" style={{marginTop:"0px", marginBottom:"-10px"}}>
                <ol className="breadcrumb">
                    <li className="breadcrumb-item active"><h2> DQR </h2></li>
                </ol>
            </div>

            <Card className={classes.root}>
                <CardContent>

                    <div className="col-xl-12 col-lg-12">
                        <Form >
                            <h2>Data Completeness Scorecard</h2>
                            <div className="card">
                                <div className="card-header" style={{backgroundColor:"#014d88",color:'#fff',fontWeight:'bolder',  borderRadius:"0.2rem"}}>
                                    <h5 className="card-title" style={{color:'#fff'}}>Demographics Variable</h5>
                                    {showDemographics===false  ? (<><span className="float-end" style={{cursor: "pointer"}} onClick={onClickDemographics}><FaPlus /></span></>) :  (<><span className="float-end" style={{cursor: "pointer"}} onClick={onClickDemographics}><FaAngleDown /></span> </>)}
                                </div>
                                {showDemographics && (
                                    <Demographic /> 
                                )}

                            </div>
                            <div className="card">
                                <div className="card-header" style={{backgroundColor:"#014d88",color:'#fff',fontWeight:'bolder',  borderRadius:"0.2rem"}}>
                                    <h5 className="card-title" style={{color:'#fff'}}>Clinical Variables</h5>
                                    {showClinical===false  ? (<><span className="float-end" style={{cursor: "pointer"}} onClick={onClickClinical}><FaPlus /></span></>) :  (<><span className="float-end" style={{cursor: "pointer"}} onClick={onClickClinical}><FaAngleDown /></span> </>)}
                                </div>
                                {showClinical && (
                                    <Clinical /> 
                                )}
                            </div>

                            <h2>Data Consistency Scorecard</h2>
                            <div className="card">
                                <div className="card-header" style={{backgroundColor:"#014d88",color:'#fff',fontWeight:'bolder',  borderRadius:"0.2rem"}}>
                                    <h5 className="card-title" style={{color:'#fff'}}>Clinicals</h5>
                                    {showClinicals===false  ? (<><span className="float-end" style={{cursor: "pointer"}} onClick={onClickClinicals}><FaPlus /></span></>) :  (<><span className="float-end" style={{cursor: "pointer"}} onClick={onClickClinicals}><FaAngleDown /></span> </>)}
                                </div>
                                {showClinicals && (
                                    <Clinicals /> 
                                )}
                            </div>

                            <div className="card">

                                <div className="card-header" style={{backgroundColor:"#014d88",color:'#fff',fontWeight:'bolder',  borderRadius:"0.2rem"}}>
                                    <h5 className="card-title" style={{color:'#fff'}}>Pharmacy</h5>
                                    {showPharmacy===false  ? (<><span className="float-end" style={{cursor: "pointer"}} onClick={onClickPharmacy}><FaPlus /></span></>) :  (<><span className="float-end" style={{cursor: "pointer"}} onClick={onClickPharmacy}><FaAngleDown /></span> </>)}
                                </div>
                                {showPharmacy && (
                                    <Pharmacy /> 
                                )}
                            </div>
                            
                            <div className="card">
                                <div className="card-header" style={{backgroundColor:"#014d88",color:'#fff',fontWeight:'bolder',  borderRadius:"0.2rem"}}>
                                    <h5 className="card-title" style={{color:'#fff'}}>Laboratory</h5>
                                    {showLaboratory===false  ? (<><span className="float-end" style={{cursor: "pointer"}} onClick={onClickLaboratory}><FaPlus /></span></>) :  (<><span className="float-end" style={{cursor: "pointer"}} onClick={onClickLaboratory}><FaAngleDown /></span> </>)}
                                </div>
                                {showLaboratory && (
                                    <Laboratory /> 
                                )}

                            </div>
                            {/* End TPT MONITORING */}
                            {/* End Nutritional Status Assessment */}
                            <div className="card">
                                <div className="card-header" style={{backgroundColor:"#014d88",color:'#fff',fontWeight:'bolder',  borderRadius:"0.2rem"}}>
                                    <h5 className="card-title" style={{color:'#fff'}}>EAC</h5>
                                    {showEac===false  ? (<><span className="float-end" style={{cursor: "pointer"}} onClick={onClickEac}><FaPlus /></span></>) :  (<><span className="float-end" style={{cursor: "pointer"}} onClick={onClickEac}><FaAngleDown /></span> </>)}
                                    
                                </div>
                                {showEac && (
                                    <EAC /> 
                                )}

                            </div>
                            <div className="card">
                                <div className="card-header" style={{backgroundColor:"#014d88",color:'#fff',fontWeight:'bolder',  borderRadius:"0.2rem"}}>
                                    <h5 className="card-title" style={{color:'#fff'}}>PrEP</h5>
                                    {showPrep===false  ? (<><span className="float-end" style={{cursor: "pointer"}} onClick={onClickPrep}><FaPlus /></span></>) :  (<><span className="float-end" style={{cursor: "pointer"}} onClick={onClickPrep}><FaAngleDown /></span> </>)}
                                </div>
                                    {showPrep && (
                                    <PrEP /> 
                                )}
                            </div>
                            <div className="card">
                                <div className="card-header" style={{backgroundColor:"#014d88",color:'#fff',fontWeight:'bolder',  borderRadius:"0.2rem"}}>
                                    <h5 className="card-title" style={{color:'#fff'}}>HTS</h5>
                                    {showHts===false  ? (<><span className="float-end" style={{cursor: "pointer"}} onClick={onClickHts}><FaPlus /></span></>) :  (<><span className="float-end" style={{cursor: "pointer"}} onClick={onClickHts}><FaAngleDown /></span> </>)}
                                </div>
                                {showHts && (
                                    <Hts /> 
                                )}
                            </div>
                            <div className="card">
                                <div className="card-header" style={{backgroundColor:"#014d88",color:'#fff',fontWeight:'bolder',  borderRadius:"0.2rem"}}>
                                    <h5 className="card-title" style={{color:'#fff'}}>TB</h5>
                                    {showTbScreening===false  ? (<><span className="float-end" style={{cursor: "pointer"}} onClick={onClickTbScreening}><FaPlus /></span></>) :  (<><span className="float-end" style={{cursor: "pointer"}} onClick={onClickTbScreening}><FaAngleDown /></span> </>)}
                                {/* console.log("Here*********") */}
                                </div>
                                {showTbScreening && (
                                    <TbScreening /> 
                                )}
                            </div>
                            <div className="card">
                                <div className="card-header" style={{backgroundColor:"#014d88",color:'#fff',fontWeight:'bolder',  borderRadius:"0.2rem"}}>
                                    <h5 className="card-title" style={{color:'#fff'}}>Biometric</h5>
                                    {showBiometric===false  ? (<><span className="float-end" style={{cursor: "pointer"}} onClick={onClickBiometric}><FaPlus /></span></>) :  (<><span className="float-end" style={{cursor: "pointer"}} onClick={onClickBiometric}><FaAngleDown /></span> </>)}
                                </div>
                                {showBiometric && (
                                  <Biometric />
                                 )}
                            </div>
                            <h2>Data Validity Scorecard</h2>
                            <div className="card">
                                <div className="card-header" style={{backgroundColor:"#014d88",color:'#fff',fontWeight:'bolder',  borderRadius:"0.2rem"}}>
                                    <h5 className="card-title" style={{color:'#fff'}}>Data Validation Report</h5>
                                    {showValidityReport===false  ? (<><span className="float-end" style={{cursor: "pointer"}} onClick={onClickValidityReport}><FaPlus /></span></>) :  (<><span className="float-end" style={{cursor: "pointer"}} onClick={onClickValidityReport}><FaAngleDown /></span> </>)}
                                </div>
                                {showValidityReport && (
                                  <ValidityReport />
                                 )}

                            </div>
                            <br />

                        </Form>
                    </div>
                </CardContent>
            </Card>

        </>
    );
};

export default DQAList