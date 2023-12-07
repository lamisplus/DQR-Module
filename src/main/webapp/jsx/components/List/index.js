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
    const [showNutrition, setShowNutrition] = useState(false);
    const [showGenderBase, setShowGenderBase] = useState(false);
    const [showChronicCondition, setShowChronicCondition] = useState(false);
    const [showPositiveHealth, setShowPositiveHealth] = useState(false);
    const [showReproductive, setShowReproductive] = useState(false);
    const [showTb, setShowTb] = useState(false);//Tpt
    const [showTpt, setShowTpt] = useState(false);

    const onClickDemographics =() =>{
        setShowDemographic(!showDemographics)
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
    const onClickPositiveHealth =() =>{
        setShowPositiveHealth(!showPositiveHealth)
    }
    const onClickReproductive =() =>{
        setShowReproductive(!showReproductive)
    }
    const onClickTpt =() =>{
        setShowTpt(!showTpt)
    }

    return (
        <>
            <ToastContainer autoClose={3000} hideProgressBar />
            <div className="row page-titles mx-0" style={{marginTop:"0px", marginBottom:"-10px"}}>
                <ol className="breadcrumb">
                    <li className="breadcrumb-item active"><h2> DQA </h2></li>
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
                                    {showTpt===false  ? (<><span className="float-end" style={{cursor: "pointer"}} onClick={onClickTpt}><FaPlus /></span></>) :  (<><span className="float-end" style={{cursor: "pointer"}} onClick={onClickTpt}><FaAngleDown /></span> </>)}
                                </div>

                            </div>
                            {/* End TPT MONITORING */}
                            {/* End Nutritional Status Assessment */}
                            <div className="card">
                                <div className="card-header" style={{backgroundColor:"#014d88",color:'#fff',fontWeight:'bolder',  borderRadius:"0.2rem"}}>
                                    <h5 className="card-title" style={{color:'#fff'}}>EAC</h5>
                                    {showNutrition===false  ? (<><span className="float-end" style={{cursor: "pointer"}} onClick={onClickNutrition}><FaPlus /></span></>) :  (<><span className="float-end" style={{cursor: "pointer"}} onClick={onClickNutrition}><FaAngleDown /></span> </>)}
                                </div>

                            </div>
                            <div className="card">
                                <div className="card-header" style={{backgroundColor:"#014d88",color:'#fff',fontWeight:'bolder',  borderRadius:"0.2rem"}}>
                                    <h5 className="card-title" style={{color:'#fff'}}>PrEP</h5>
                                    {showNutrition===false  ? (<><span className="float-end" style={{cursor: "pointer"}} onClick={onClickNutrition}><FaPlus /></span></>) :  (<><span className="float-end" style={{cursor: "pointer"}} onClick={onClickNutrition}><FaAngleDown /></span> </>)}
                                </div>

                            </div>
                            <div className="card">
                                <div className="card-header" style={{backgroundColor:"#014d88",color:'#fff',fontWeight:'bolder',  borderRadius:"0.2rem"}}>
                                    <h5 className="card-title" style={{color:'#fff'}}>HTS</h5>
                                    {showNutrition===false  ? (<><span className="float-end" style={{cursor: "pointer"}} onClick={onClickNutrition}><FaPlus /></span></>) :  (<><span className="float-end" style={{cursor: "pointer"}} onClick={onClickNutrition}><FaAngleDown /></span> </>)}
                                </div>

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
                                    {showNutrition===false  ? (<><span className="float-end" style={{cursor: "pointer"}} onClick={onClickNutrition}><FaPlus /></span></>) :  (<><span className="float-end" style={{cursor: "pointer"}} onClick={onClickNutrition}><FaAngleDown /></span> </>)}
                                </div>

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