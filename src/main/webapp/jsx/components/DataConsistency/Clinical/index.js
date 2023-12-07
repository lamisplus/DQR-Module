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


    const Clinicals = (props) => {
    const classes = useStyles();
    const [clinicals, setClinical] = useState({});
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

      const loadClinical = () => {
        axios
          .get(`${baseUrl}dqr/data-consistency-summary?facilityId=${facilities}`, {
            headers: { Authorization: `Bearer ${token}` },
          })
          .then((response) => {
            setClinical(response.data);
            console.log(response.data)
          })
          .catch((error) => {
            console.log(error);
          });
      };

      useEffect(() => {
        Facilities();
        loadClinical();
      }, []);


    return (
        <>
           
            <Card className={classes.root}>
                <CardContent>
                    <h3>Clinicals</h3>
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
                                    Proportion of all active patients without documented target group
                                </td>
                                 <td>{clinicals[0]?.targNumerator}</td>
                                 <td>{clinicals[0]?.targDenominator}</td>
                                 <td>{clinicals[0]?.targPerformance} %</td>
                                <td>
                                    
                                </td>
                            </tr>
                            <tr>
                                <th scope="row">
                                    2
                                </th>
                                <td>
                                    Proportion of all active patients with a documented care entry point
                                </td>
                                <td>{clinicals[0]?.entryNumerator}</td>
                                <td>{clinicals[0]?.entryDenominator}</td>
                                <td>{clinicals[0]?.entryPerformance} %</td>
                                <td>
                                    
                                </td>
                            </tr>
                            <tr>
                                <th scope="row">
                                    3
                                </th>
                                <td>
                                    Proportion of all active patients with documented abnormal weight of 121 and above
                                </td>
                                <td>{clinicals[0]?.adultWeightNumerator}</td>
                                <td>{clinicals[0]?.adultWeightDenominator}</td>
                                <td>{clinicals[0]?.adultWeightPerformance} %</td>
                                <td>
                                    
                                </td>
                            </tr>
                            <tr>
                                <th scope="row">
                                    4
                                </th>
                                <td>
                                    Proportions of all active paediatric patients age 0 – 14 on ART that had documented weight of 61 and above
                                </td>
                                <td>{clinicals[0]?.peadWeightNumerator}</td>
                                <td>{clinicals[0]?.peadWeightDenominator}</td>
                                <td>{clinicals[0]?.peadWeightPerformance} %</td>
                                <td>
                                    
                                </td>
                            </tr>
                            <tr>
                                <th scope="row">
                                    5
                                </th>
                                <td>
                                    Proportion of all active female patient 12 year and above with a documented pregnancy status
                                </td>
                                <td>{clinicals[0]?.pregNumerator}</td>
                                <td>{clinicals[0]?.pregDenominator}</td>
                                <td>{clinicals[0]?.pregPerformance} %</td>
                                <td>
                                    
                                </td>
                            </tr>
                            <tr>
                                <th scope="row">
                                    6
                                </th>
                                <td>
                                    Proportion of all active patients with an ART start date on or before the current calendar date
                                </td>
                               <td>{clinicals[0]?.artDateLessTodayDenominator}</td>
                               <td>{clinicals[0]?.artDateLessTodayNumerator}</td>
                               <td>{clinicals[0]?.artDateLessTodayPerformance} %</td>
                                <td>
                                    
                                </td>
                            </tr>
                            <tr>
                                <th scope="row">
                                    7
                                </th>
                                <td>
                                    Proportion of all active patients with an ART start date on or before last clinic visit date
                                </td>
                                <td>{clinicals[0]?.artEqClinicNumerator}</td>
                                <td>{clinicals[0]?.artEqClinicDenominator}</td>
                                <td>{clinicals[0]?.artEqClinicPerformance} %</td>
                                <td>
                                    
                                </td>
                            </tr>
                            <tr>
                                <th scope="row">
                                    8
                                </th>
                                <td>
                                    Proportion of all active patients with an ART start date on or before last drug pickup date
                                </td>
                                <td>{clinicals[0]?.artEqLastPickupNumerator}</td>
                                <td>{clinicals[0]?.artEqLastPickupDenominator}</td>
                                <td>{clinicals[0]?.artEqLastPickupPerformance} %</td>
                                <td>
                                    
                                </td>
                            </tr>
                            <tr>
                              <th scope="row">
                                  9
                              </th>
                              <td>
                                  Proportion of all active patients with last drug pickup date on or after first confirmed HIV date
                              </td>
                              <td>{clinicals[0]?.lgreaterConfNumerator}</td>
                              <td>{clinicals[0]?.lgreaterConfDenominator}</td>
                              <td>{clinicals[0]?.lgreaterConfPerformance} %</td>
                              <td>

                              </td>
                              </tr>
                              <tr>
                             <th scope="row">
                                 10
                             </th>
                             <td>
                                 Proportion of all active patients with ART start date before transfer-in date
                             </td>
                             <td>{clinicals[0]?.artGreaterTransNumerator}</td>
                             <td>{clinicals[0]?.artGreaterTransDenominator}</td>
                             <td>{clinicals[0]?.artGreaterTransPerformance} %</td>
                             <td>

                             </td>
                             </tr>

                            <tr>
                            <th scope="row">
                                11
                            </th>
                            <td>
                                Proportion of all active patients with Last clinic visit date on or after first confirmed HIV date
                            </td>
                            <td>{clinicals[0]?.lgreaterConfNumerator}</td>
                            <td>{clinicals[0]?.lgreaterConfDenominator}</td>
                            <td>{clinicals[0]?.lgreaterConfPerformance} %</td>
                            <td>

                            </td>
                            </tr>

                                <tr>
                                <th scope="row">
                                    12
                                </th>
                                <td>
                                    Proportion of all active patients with Last drug pickup date after date of birth
                                </td>
                                <td>{clinicals[0]?.lstPickGreaterDObNumerator}</td>
                                <td>{clinicals[0]?.lstPickGreaterDObDenominator}</td>
                                <td>{clinicals[0]?.lstPickGreaterDObPerformance} %</td>
                                <td>

                                </td>
                            </tr>
                            <tr>
                            <th scope="row">
                                13
                            </th>
                            <td>
                                Proportion of all active patients Newly initiated on ART (TX_NEW) in the quarter but has previous quarter drug pickup date
                            </td>
                            <td> N/A</td>
                            <td>N/A</td>
                            <td> %</td>
                            <td>

                            </td>
                            </tr>
                            <tr>
                            <th scope="row">
                                14
                            </th>
                            <td>
                                Proportion of all active patients with Last drug pickup date on or after transferred in date
                            </td>
                            <td>{clinicals[0]?.ldrugPickHighNumerator}</td>
                            <td>{clinicals[0]?.ldrugPickHighDenominator}</td>
                            <td>{clinicals[0]?.ldrugPickHighPerformance} %</td>
                            <td>

                            </td>
                            </tr>
                            <tr>
                             <th scope="row">
                                 15
                             </th>
                             <td>
                                 Proportion of all active patients with Last drug pickup date on or before current calendar date
                             </td>
                             <td>{clinicals[0]?.clinicPickLessTodayNumerator}</td>
                             <td>{clinicals[0]?.clinicPickLessTodayDenominator}</td>
                             <td>{clinicals[0]?.clinicPickLessTodayPerformance} %</td>
                             <td>

                             </td>
                             </tr>
<tr>
                             <th scope="row">
                                 16
                             </th>
                             <td>
                                 Proportion of all active patients with Last clinic visit date on or before current calendar date
                             </td>
                             <td>{clinicals[0]?.clinicPickLessTodayNumerator}</td>
                             <td>{clinicals[0]?.clinicPickLessTodayDenominator}</td>
                             <td>{clinicals[0]?.clinicPickLessTodayPerformance} %</td>
                             <td>

                             </td>
                             </tr>
<tr>
                             <th scope="row">
                                 17
                             </th>
                             <td>
                                 Proportion of all active patients with Date of VL result after the date of VL sample collection
                             </td>
                             <td></td>
                             <td></td>
                             <td> %</td>
                             <td>

                             </td>
                             </tr>
<tr>
                             <th scope="row">
                                 18
                             </th>
                             <td>
                                 Proportion of new patients (TX_NEW) with CD4 count
                             </td>
                             <td></td>
                             <td></td>
                             <td> %</td>
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

export default Clinicals