import React, { useEffect, useState } from "react";
import axios from "axios";
import { token, url as baseUrl } from "../../../../api";
import { Form, Table } from "reactstrap";
import { makeStyles } from "@material-ui/core/styles";
import { Card, CardContent } from "@material-ui/core";
import "semantic-ui-css/semantic.min.css";
import { Dropdown, Button as Buuton2, Menu, Icon } from "semantic-ui-react";
import CloudUpload from "@material-ui/icons/CloudUpload";
import CloudDownloadIcon from "@material-ui/icons/CloudDownload";
import ErrorIcon from "@mui/icons-material/Error";

import { FiUploadCloud } from "react-icons/fi";
import "react-toastify/dist/ReactToastify.css";
import "react-widgets/dist/css/react-widgets.css";



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


    const Clinical = (props) => {
    const classes = useStyles();
    const [clinical, setClinicals] = useState({});
    const [facilities, setFacilities] = useState([]);

  const Facilities = () => {
    axios
      .get(`${baseUrl}account`, {
        headers: { Authorization: `Bearer ${token}` },
      })
      .then((response) => {
        setFacilities(response.data.currentOrganisationUnitId);
      })
      .catch((error) => {
        console.log(error);
      });
  };

  const loadClinicals = () => {
    axios
      .get(`${baseUrl}dqr/clinical-variable-summary?facilityId=${facilities}`, {
        headers: { Authorization: `Bearer ${token}` },
      })
      .then((response) => {
        setClinicals(response.data);
        console.log(response.data)
      })
      .catch((error) => {
        console.log(error);
      });
  };

  useEffect(() => {
    Facilities();
    loadClinicals();
  }, []);


    return (
        <>
           
            <Card className={classes.root}>
                <CardContent>
                    <h3>Clinical Variables</h3>
                    <div className="col-xl-12 col-lg-12">
                        <Table bordered>
                            <thead>
                            <tr>
                                <th>
                                    #
                                </th>
                                <th>
                                    Complete Variables
                                </th>
                                <th>
                                    Numerator
                                </th>
                                <th>
                                  Denominator
                                </th>
                                <th>
                                  Performance
                                </th>
                                <th>
                                    Action
                                </th>
                            </tr>
                            </thead>
                            <tbody>
                            <tr>
                                <th scope="row">
                                    1
                                </th>
                                <td>
                                    Proportion of all active patients with documented month of ARV refill
                                </td>
                                <td>{clinical[0]?.refillMonthNumerator}</td>
                                <td>{clinical[0]?.refillMonthDenominator}</td>
                                <td>{clinical[0]?.refillMonthPerformance} %</td>
                                <td>
                                    
                                </td>
                            </tr>
                            <tr>
                                <th scope="row">
                                    2
                                </th>
                                <td>
                                    Proportion of all active patients with ART Start Date
                                </td>
                                <td>{clinical[0]?.startDateNumerator}</td>
                                <td>{clinical[0]?.startDateDenominator}</td>
                                <td>{clinical[0]?.startDatePerformance} %</td>
                                <td>
                                    
                                </td>
                            </tr>
                            <tr>
                                <th scope="row">
                                    3
                                </th>
                                <td>
                                    Proportion of all active patients with First HIV confirmed test Date
                                </td>
                                <td>{clinical[0]?.confirmDateNumerator}</td>
                                <td>{clinical[0]?.confirmDateDenominator}</td>
                                <td>{clinical[0]?.confirmDatePerformance} %</td>
                                <td>
                                    
                                </td>
                            </tr>
                            <tr>
                                <th scope="row">
                                    4
                                </th>
                                <td>
                                    Proportion of all active patients with documented drug pickup date
                                </td>
                                <td>{clinical[0]?.lastPickNumerator}</td>
                                <td>{clinical[0]?.lastPickDenominator}</td>
                                <td>{clinical[0]?.lastPickPerformance} %</td>
                                <td>
                                    
                                </td>
                            </tr>
                            <tr>
                                <th scope="row">
                                    5
                                </th>
                                <td>
                                    Proportion of all active patients with Age at ART Initiation
                                </td>
                               <td>{clinical[0]?.ageNumerator}</td>
                               <td>{clinical[0]?.ageDenominator}</td>
                               <td>{clinical[0]?.agePerformance} %</td>
                                <td>
                                    
                                </td>
                            </tr>
                            <tr>
                                <th scope="row">
                                    6
                                </th>
                                <td>
                                    Proportion of all active patients with Last Drug Regimen
                                </td>
                                <td>{clinical[0]?.regimenNumerator}</td>
                                <td>{clinical[0]?.regimenDenominator}</td>
                                <td>{clinical[0]?.regimenPerformance} %</td>
                                <td>
                                    
                                </td>
                            </tr>
                            <tr>
                                <th scope="row">
                                    7
                                </th>
                                <td>
                                    Proportion of all active patients with documented target group
                                </td>
                                <td>{clinical[0]?.targNumerator}</td>
                                <td>{clinical[0]?.targDenominator}</td>
                                <td>{clinical[0]?.targPerformance} %</td>;
                                <td>
                                    
                                </td>
                            </tr>
                            <tr>
                                <th scope="row">
                                    8
                                </th>
                                <td>
                                Proportion of all active patients with a documented care entry point
                                </td>
                                <td>{clinical[0]?.entryNumerator}</td>
                                <td>{clinical[0]?.entryDenominator}</td>
                                <td>{clinical[0]?.entryPerformance} %</td>
                                <td>
                                    
                                </td>
                            </tr>
                            <tr>
                       <th scope="row">
                           9
                       </th>
                               <td>
                               Proportion of all active patients with Last Drug Regimen Code
                               </td>
                               <td></td>
                               <td></td>
                               <td> %</td>
                               <td>

                               </td>
                        </tr>
                        <tr>
                                <th scope="row">
                                    10
                                </th>
                                <td>
                                Proportion of all active patients that had documented last Clinic visit date
                                </td>
                                <td>{clinical[0]?.lastVisitNumerator}</td>
                                <td>{clinical[0]?.lastVisitDenominator}</td>
                                <td>{clinical[0]?.lastVisitPerformance} %</td>
                                <td>

                                </td>
                            </tr>
                            <tr>
                                <th scope="row">
                                    11
                                </th>
                                <td>
                                Proportion of all active patients with documented weight
                                </td>
                                <td>{clinical[0]?.weightNumerator}</td>
                                <td>{clinical[0]?.weightDenominator}</td>
                                <td>{clinical[0]?.weightPerformance} %</td>
                                <td>

                                </td>
                            </tr>
                            <tr>
                                <th scope="row">
                                    12
                                </th>
                                <td>
                                Proportion of all active female >12 age with Pregnancy status at last visit
                                </td>
                               <td>{clinical[0]?.pregNumerator}</td>
                               <td>{clinical[0]?.pregDenominator}</td>
                               <td>{clinical[0]?.pregPerformance} %</td>
                                <td>

                                </td>
                             </tr>
                             <tr>
                                <th scope="row">
                                    13
                                </th>
                                <td>
                                Proportion of all active patients with documented dates of HIV diagnosis
                                </td>
                                <td>{clinical[0]?.diagnoseNumerator}</td>
                                <td>{clinical[0]?.diagnoseDenominator}</td>
                                <td>{clinical[0]?.diagnosePerformance} %</td>
                                <td>

                                </td>
                             </tr>
                           <tr>
                                <th scope="row">
                                    14
                                </th>
                                <td>
                                Proportion of all active patients with documented HIV enrolment date
                                </td>
                                <td>{clinical[0]?.enrolledDateNumerator}</td>
                                <td>{clinical[0]?.enrolledDateDenominator}</td>
                                <td>{clinical[0]?.enrolledDatePerformance} %</td>
                                <td>

                                </td>
                              </tr>
                               <tr>
                               <th scope="row">
                                   15
                               </th>
                               <td>
                               Proportion of all active patients with documented ART Commencement date
                               </td>
                               <td>{clinical[0]?.commencedNumerator}</td>
                               <td>{clinical[0]?.commencedDenominator}</td>
                               <td>{clinical[0]?.commencedPerformance} %</td>
                               <td>

                               </td>
                             </tr>
                            </tbody>
                        </Table>
                    </div>
                </CardContent>
            </Card>

        </>
    );
};

export default Clinical